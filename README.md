RemoteControls
==========

PhoneGap / Cordova iOS plugin that allows you to use the Remote Controls as well as add metadatas in RemoteControlsInfoCenter

Require
-------

- PhoneGap / Cordova 3.x
- metadata portion forked from of https://github.com/silverorange/RemoteControls

Installation
------------

Add the plugin much like any other:

1. Add MediaPlayer.framework in your project.
2. Add the RemoteControls.h and RemoteControls.m classes to your Plugins folder in Xcode
3. Add the RemoteControls.js file to your www folder
4. Add the RemoteControls.js to your html file. eg: `<script type="text/javascript" charset="utf-8" src="RemoteControls.js"></script>`
5. Add the plugin to your config.xml: `<plugin name="RemoteControls" value="RemoteControls" />` (or if you are running an older version of PhoneGap / Cordova, Cordova.plist under Plugins (key: "RemoteControls" value: "RemoteControls"))
6. In MainViewController.m add to -(void)viewDidLoad [[RemoteControls remoteControls] setWebView:self.webView];
7. Add this function to MainViewController.m
- (void)remoteControlReceivedWithEvent:(UIEvent *)receivedEvent {

       [[RemoteControls remoteControls] receiveRemoteEvent:receivedEvent];
   }
   

### Example
```javascript
function onDeviceReady() {
  var RemoteControls = window.plugins.RemoteControls;
  artist = "Daft Punk";
  title = "One More Time";
  album = "Discovery";
  RemoteControls.updateMetas(artist,title,station);
}
```

## RemoteControls License

The MIT License

Copyright (c) 2013 Seth Hillinger (http://github.com/shi11)

## Metadata License

The MIT License

Copyright (c) 2013 François LASSERRE (http://github.com/choiz)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
