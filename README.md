## 用mirai+springboot开发的qq机器人

### 功能介绍
权限检验管理，点歌，随机发图，分享网易云音乐，发语音，防撤回，下棋，加群退群提示，禁言关键词，给群友头像生成表情包，动态禁用指令，构造假转发消息，生成二维码等  
  
__ChatGPT集成__
__bilibili动态订阅__
__Vits语音合成__
__人生重开游戏__
__灯神猜人物__
***

### 预先配置(重要)
#### 功能配置
|  功能   | 配置方法  |
|  ----  | ----  |
| ChatGPT  | 需要配置chatApiKey，另外国内需要代理，可在配置文件里设置或者参考https://github.com/noobnooc/noobnooc/discussions/9 |
| Vits语音合成  | 合成软件: https://github.com/CjangCjengh/MoeGoe 模型下载: https://github.com/CjangCjengh/TTSModels 语音模型用的是Nene + Nanami + Rong + Tang， 情感模型用的是W2V2-VITS， 下载后修改配置文件对应路径 |
|  人生重开游戏  | 下载仓库https://github.com/cc004/lifeRestart-py  并成功运行，修改配置里执行对应python脚本的命令  |
|  灯神猜人物  | 需要翻墙，可在配置文件里设置代理  |
|  网易云点歌  | 由于网易云音乐api需要登录才能用，需要配置cookie  |

#### 配置文件修改

__重要__: 必须将resource目录下的配置文件 __application_temp.properties__ 改名为 __application.properties__
#### 环境配置
maven 3.6.3

jdk版本 __17__

kotlin __1.6__

jdk和kotlin版本比这低很可能跑不起来

操作系统: __windows__
由于用到了音频转换，需要 __ffmpeg__
***

### 新功能开发方法
添加新功能可以使用@handler @HandlerComponent @Permission注解
***

### 其他
防撤回功能注释掉了，要使用在MessageRecallEvents文件中去掉注释即可。

FixProtocolVersion用于修复临时无法登录，mirai出新版本后可以去掉
