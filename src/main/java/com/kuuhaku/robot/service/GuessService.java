package com.kuuhaku.robot.service;

import com.github.markozajc.akiwrapper.Akiwrapper;
import com.github.markozajc.akiwrapper.AkiwrapperBuilder;
import com.github.markozajc.akiwrapper.core.entities.Guess;
import com.github.markozajc.akiwrapper.core.entities.Question;
import com.github.markozajc.akiwrapper.core.entities.Server.GuessType;
import com.github.markozajc.akiwrapper.core.entities.Server.Language;
import com.github.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import com.kuuhaku.robot.config.ProxyConfig;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.markozajc.akiwrapper.Akiwrapper.Answer.*;
import static com.github.markozajc.akiwrapper.core.entities.Server.GuessType.CHARACTER;
import static com.github.markozajc.akiwrapper.core.entities.Server.Language.ENGLISH;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.joining;

@Service
@Slf4j
public class GuessService {
    public static final ConcurrentHashMap<Long, GuessGame> map = new ConcurrentHashMap<>();
    public static final double PROBABILITY_THRESHOLD = 0.85;
    public static final ReentrantLock lock = new ReentrantLock();
    @Autowired
    public DownloadService downloadService;
    @Autowired
    public ImageService imageService;
    @Autowired
    public ProxyConfig proxyConfig;

    // This is used to determine which guesses are probable (probability is determined by
    // Akinator) enough to propose to the player.
    public GuessGame newInstance(Contact contact) {
        lock.lock();
        if (map.containsKey(contact.getId())) {
            lock.unlock();
            return null;
        }
        GuessGame guessGame = new GuessGame(contact);
        map.put(contact.getId(), guessGame);
        lock.unlock();
        return guessGame;
    }

    public void sendMsg(Long groupID, String msg) {
        GuessGame guessGame = map.get(groupID);
        if (guessGame == null) {
            return;
        }
        if (msg.equals("stop")) {
            guessGame.setStopped();
            map.remove(groupID);
        }
        guessGame.sendMsg(msg);
    }

    public class GuessGame {
        private final LinkedBlockingQueue<String> in = new LinkedBlockingQueue<>();
        private final Contact contact;
        private final Long id;
        private boolean stopped = false;

        public GuessGame(Contact contact) {
            this.contact = contact;
            id = contact.getId();
        }

        public String getMsg() {
            try {
                return in.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }

        private void removeSelf() {
            lock.lock();
            map.remove(id);
            lock.unlock();
        }

        public void setStopped() {
            stopped = true;
        }


        public void start() {
            try {
                boolean filterProfanity = getProfanityFilter();
                if (stopped) {
                    return;
                }
                // Gets player's age. Like the Akinator's website, this will turn on the profanity
                // filter if the age entered is below 16.

                var language = getLanguage();
                if (stopped) {
                    return;
                }
                // Gets player's language. Akinator will give the user localized questions and
                // guesses depending on user's language.

                var guessType = getGuessType();
                if (stopped) {
                    return;
                }
                // Gets the guess type.
                Akiwrapper aw;
                try {
                    assert language != null;
                    assert guessType != null;
                    aw = new AkiwrapperBuilder().setFilterProfanity(filterProfanity)
                            .setLanguage(language)
                            .setGuessType(guessType)
                            .build();
                } catch (ServerNotFoundException e) {
                    contact.sendMessage("Unsupported combination of language and guess type");
                    removeSelf();
                    return;
                }
                // Builds the Akiwrapper instance, this is what we'll be using to perform
                // operations such as answering questions, fetching guesses, etc

                var rejected = new ArrayList<Long>();
                // A list of rejected guesses, used to prevent them from repeating

                while (aw.getQuestion() != null) {
                    // Runs while there are still questions left

                    Question question = aw.getQuestion();
                    if (question == null)
                        break;
                    // Breaks the loop if question is null; /should/ not occur, but safety is still
                    // first
                    contact.sendMessage(String.format("Question #%d%n", question.getStep() + 1) + "\n" + String.format("\t%s%n", question.getQuestion()));
                    // Displays the question.

                    if (question.getStep() == 0)

                        contact.sendMessage("Answer with " +
                                "Y (yes), N (no), DK (don't know), P (probably) or PN (probably not) " +
                                "or go back in time with B (back).");
                    // Displays the tip (only for the first time)

                    answerQuestion(aw);
                    if (stopped) {
                        return;
                    }
                    if (reviewGuesses(aw, rejected)) {
                        return;
                    }
                    // Iterates over any available guesses.
                }

                for (Guess guess : aw.getGuesses()) {
                    if (reviewGuess(guess)) {
                        // Reviews all final guesses.
                        if (stopped) {
                            return;
                        }
                        finish(true);
                        removeSelf();
                        return;
                    }
                }

                finish(false);
                removeSelf();
                // Loses if all guesses are rejected.
            } catch (Exception e) {
                contact.sendMessage("error, game over");
                e.printStackTrace();
            } finally {
                removeSelf();
            }
        }

        public void sendMsg(String msg) {
            in.add(msg);
        }


        private void answerQuestion(Akiwrapper aw) {
            boolean answered = false;
            while (!answered) {
                // Iterates while the questions remains unanswered.

                var answer = getMsg().toLowerCase();
                if (answer.equals("stop")) {
                    contact.sendMessage("game over");
                    return;
                }


                switch (answer) {
                    case "y" -> aw.answer(YES);
                    case "n" -> aw.answer(NO);
                    case "dk" -> aw.answer(DONT_KNOW);
                    case "p" -> aw.answer(PROBABLY);
                    case "pn" -> aw.answer(PROBABLY_NOT);
                    case "b" -> aw.undoAnswer();
                    case "debug" -> {
                        contact.sendMessage(String.format("Debug information:%n\tCurrent API server: %s%n\tCurrent guess count: %d%n",
                                aw.getServer().getUrl(), aw.getGuesses().size()));
                        continue;
                    }
                    // Displays some debug information.

                    default -> {
                        contact.sendMessage("Please answer with either " +
                                "[Y]ES, [N]O, [D|ONT |K]NOW, [P]ROBABLY or [P|ROBABLY |N]OT or go back one step with [B]ACK.");
                        continue;
                    }
                }

                answered = true;
                // Answers the question.
            }
        }

        private boolean reviewGuess(Guess guess) {
            contact.sendMessage(guess.getName());
            MessageChain chain = MessageUtils.newChain();
            Image image = null;
            String path = downloadService.getRandomPngPath();
            try {
                if (guess.getImage() != null) {
                    boolean userProxy = proxyConfig.getProtocol() != null;
                    downloadService.download(guess.getImage().toString(), path, userProxy);
                }
                image = imageService.uploadImage(path, contact);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (image != null) {
                chain = chain.plus(image);
            }
            downloadService.deleteFile(path);
            if (guess.getDescription() == null)
                contact.sendMessage("(no description)");
            else
                contact.sendMessage(chain.plus(guess.getDescription()));
            // Displays the guess' information

            boolean answered = false;
            boolean isCharacter = false;
            while (!answered) {
                // Asks the player if the guess is correct

                contact.sendMessage("Is this your character? (y/n)");
                String line = getMsg().toLowerCase();
                if (line.equals("stop")) {
                    contact.sendMessage("game over");
                    return false;
                }
                switch (line) {
                    case "y":
                        // If the player has responded positively.
                        answered = true;
                        isCharacter = true;
                        break;

                    case "n":
                        // If the player has responded negatively.
                        answered = true;
                        isCharacter = false;
                        break;

                    default:
                        break;
                }
            }

            return isCharacter;
        }

        private boolean reviewGuesses(Akiwrapper aw, List<Long> declined) {
            for (var guess : aw.getGuessesAboveProbability(PROBABILITY_THRESHOLD)) {
                if (!declined.contains(guess.getIdLong())) {
                    // Checks if this guess complies with the conditions.

                    if (reviewGuess(guess)) {
                        // If the player accepts this guess
                        if (stopped) {
                            return true;
                        }
                        finish(true);
                        removeSelf();
                        return true;
                    }

                    declined.add(guess.getIdLong());
                    // Registers this guess as rejected.
                }
            }
            return false;
        }

        private void finish(boolean win) {
            if (win) {
                // If Akinator has won.
                contact.sendMessage("Great!");
                contact.sendMessage("\tGuessed right one more time. I love playing with you!");
            } else {
                // If the player has won.
                contact.sendMessage("Bravo!");
                contact.sendMessage("\tYou have defeated me.");
            }
        }

        private boolean getProfanityFilter() {
            contact.sendMessage("What's your age? (default: 18)");
            while (true) {
                var age = getMsg();
                if (age.equals("stop")) {
                    contact.sendMessage("game over");
                    return false;
                }

                if (age.equals(""))
                    return false;

                try {
                    return parseInt(age) < 16;
                } catch (NumberFormatException e) {
                    contact.sendMessage("That's not a number");
                }
            }
        }


        private Language getLanguage() {
            var languages = EnumSet.allOf(Language.class);

            contact.sendMessage("What's your language? (default: English)");
            while (true) {
                String selectedLanguage = getMsg().toLowerCase().trim();
                if (selectedLanguage.equals("stop")) {
                    contact.sendMessage("game over");
                    return null;
                }


                if (selectedLanguage.equals(""))
                    return ENGLISH;

                var language = languages.stream()
                        .filter(l -> l.toString().toLowerCase().equals(selectedLanguage))
                        .findAny()
                        .orElse(null);

                if (language != null) {
                    return language;

                } else {
                    contact.sendMessage(languages.stream()
                            .map(Enum::toString)
                            .collect(joining("\n-", "Sorry, that language isn't supported. Choose between\n-", "")));
                }
            }
        }


        private GuessType getGuessType() {
            var guessTypes = EnumSet.allOf(GuessType.class);

            contact.sendMessage("What will you be guessing? (default: character)");
            while (true) {
                String selectedGuessType = getMsg().toLowerCase().trim();
                if (selectedGuessType.equals("stop")) {
                    contact.sendMessage("game over");
                    return null;
                }

                if (selectedGuessType.equals(""))
                    return CHARACTER;

                var guessType = guessTypes.stream()
                        .filter(l -> l.toString().toLowerCase().equals(selectedGuessType))
                        .findAny()
                        .orElse(null);

                if (guessType != null) {
                    return guessType;

                } else {
                    contact.sendMessage("" + guessTypes.stream()
                            .map(Enum::toString)
                            .collect(joining("\n-", "Sorry, that guess type isn't supported. Choose between\n-", "")));
                }
            }
        }
    }

}
