# RemoteControls

PhoneGap / Cordova plugin that allows you to use Music Remote Controls.

## Supported platforms
- Android (4.1+)
- Windows (10+)
- iOS

## Require
- PhoneGap / Cordova 3.x

## Installation

Add the plugin much like any other:

`cordova plugin add https://github.com/shi11/RemoteControls`

## Methods
- Create the media controls:
```javascript
MusicControls.create({
    track       : 'Time is Running Out',
	artist      : 'Muse',
    cover       : 'albums/absolution.jpg',
    isPlaying   : true
	album       : 'Absolution', // iOS only
	duration    : 321,			// iOS only
	elapsedTime : 123			// iOS only
}, onSuccess, onError);
```

- Destroy the media controller:
```javascript
MusicControls.destroy(onSuccess, onError);
```

- Subscribe events to the media controller:
```javascript
function events(action) {
	switch(action) {
		case 'music-controls-next':
			// Do something
			break;
		case 'music-controls-previous':
			// Do something
			break;
		case 'music-controls-pause':
			// Do something
			break;
		case 'music-controls-play':
			// Do something
			break;

		// Headset events (Android only)
		case 'music-controls-media-button' :
			// Do something
			break;
		case 'music-controls-headset-unplugged':
			// Do something
			break;
		case 'music-controls-headset-plugged':
			// Do something
			break;
		default:
			break;
	}
}

// Register callback
MusicControls.subscribe(events);

// Start listening for events
// The plugin will run the events function each time an event is fired
MusicControls.listen();
```

## iOS specific

Modify the MainViewController.m with these functions:

```
- (void)viewDidLoad
{
    [super viewDidLoad];
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
    // Do any additional setup after loading the view from its nib.
    [[RemoteControls remoteControls] setWebView:self.webView];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    // Turn off remote control event delivery
    [[UIApplication sharedApplication] endReceivingRemoteControlEvents];
}

//add this function
- (void)remoteControlReceivedWithEvent:(UIEvent *)receivedEvent {
       [[RemoteControls remoteControls] receiveRemoteEvent:receivedEvent];
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
