# qq-robot
##用mirai+springboot开发的qq机器人

## 集成功能介绍
权限检验管理，点歌，随机发图，分享网易云音乐，发语音，防撤回，下棋，加群出群提示，禁言关键词，
给群友头像生成表情包，动态禁用指令，构造假转发消息, 二维码等
#### ChatGPT集成
#### bilibili动态订阅
#### Vits语音合成
#### 人生重开游戏
#### 灯神猜人物  
  
  

### 预先配置
####功能配置
|  功能   | 配置方法  |
|  ----  | ----  |
| ChatGPT  | 配置chatApiKey，另外国内需要代理，可以用Proxy设置代理或者参考https://github.com/noobnooc/noobnooc/discussions/9 |
| Vits语音合成  | 需要下载软件https://github.com/CjangCjengh/MoeGoe, 下载模型https://github.com/CjangCjengh/TTSModels 语音模型用的是Nene + Nanami + Rong + Tang 情感模型用的是W2V2-VITS， 之后修改配置文件对应路径 |
|  人生重开游戏  | 下载仓库https://github.com/cc004/lifeRestart-py  并成功运行，修改配置改成对应执行python脚本的命令  |
|  灯神猜人物  | 如果要翻墙需要在配置文件里设置好代理  |
|  网易云点歌  | 网易云音乐api需要登录才能用，需要配置cookie  |  
  
  
  

####配置文件修改
重要: 必须将配置文件application_temp.properties改名为application.properties给springboot读取  
  

####环境配置
maven3.6.3

jdk版本 17

kotlin1.6

低于jdk和kotlin版本的环境很可能跑不起来


### 新功能开发
添加新指令可以使用@handler @HandlerComponent @Permission()
注解对应参数的值有说明
  
  
  

###其他
防止撤回功能注释掉了，需要在MessageRecallEvents文件里里自行开启。
FixProtocolVersion用于修复临时无法登录，以后可以去掉
