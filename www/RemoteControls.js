//
// RemoteControls.js
// Remote Control Cordova Plugin

// remoteControls created by Seth Hillinger on 2/21/14.
// Copyright 2013 Seth Hillinger. All rights reserved.

// updateMetas created by François LASSERRE on 12/05/13.
// Copyright 2013 François LASSERRE. All rights reserved.
// MIT Licensed
//


//------------------------------------------------------------------------------
// object that we're exporting
//------------------------------------------------------------------------------
var remoteControls = module.exports;

remoteControls.updateMetas = function(artist, title, album, cover) {
    cordova.exec(null, null, 'RemoteControls', 'updateMetas', [artist, title, album, cover]);
};

remoteControls.receiveRemoteEvent = function(event) {
    console.log("nowplaying.js receiveremoetevent asdf");
    var ev = document.createEvent('HTMLEvents');
    ev.remoteEvent = event;
    ev.initEvent('remote-event', true, true, arguments);
    document.dispatchEvent(ev);
}