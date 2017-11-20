# pgyplugin


#上传APK到蒲公英

外部gradle接入 classpath 'com.zhangniuniu.pgypluginz:pgyz-plugin:1.0.3'

项目gradle apply plugin: 'com.pgy.plugin'


会生成相应的task，组名是pgy，根据 flavor、buildType分别生成不同的task


需要加入拓展
``` json
    pgy {
         _api_key = "e8e478a631a1019ad80245712b995f40"
         uKey = "317e4d6b6bc1e0308d2fa4e6075cce40"
         updateDescription = “更新描述”
    }
``` 
