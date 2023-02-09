# flutter_image_segmentation_kotlin




## Use Huawei ML Kit
Configuration
1. Configure App following reference below
    Reference: https://medium.com/huawei-developers/android-integrating-your-apps-with-huawei-hms-core-1f1e2a090e98
2. Register App in Huawei Developer
3. Download *agconnect-services.json* file and put in /android/app/ 
4. Run Code
5. Change Image and Hot Restart app to see other picture's image

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
    -- App will crash on several Images for Pytorch
    -- Suspected Issues : Model incompatible

### Change Java Library to Below
/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home
