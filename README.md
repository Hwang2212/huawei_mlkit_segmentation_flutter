# flutter_image_segmentation_kotlin

App will crash on several Images for Pytorch


## Use Huawei ML Kit
Configuration
- Configure App following reference below
    Reference: https://medium.com/huawei-developers/android-integrating-your-apps-with-huawei-hms-core-1f1e2a090e98
- Register App in Huawei Developer
- Download *agconnect-services.json* file and put in /android/app/ 
- Run Code

### Issues about Remove Background:
- *Chaquopy using Python Package REMBG*:
    -- Tried integrating Python into Flutter, but it doesn't support onnxruntime
- *Flython*:
    -- Can't resolve open file in windows, not recommended in android
- *StarFlut*:
    -- Too many configuration steps

- *Tensorflow Lite*:
    -- Conversion steps was a hassle, 
- *Kotlin Pytorch*:
    -- Currently the best way, ongoing development

### Change Java Library to Below
/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home
