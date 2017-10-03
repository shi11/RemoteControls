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
// Cordova Objects
//------------------------------------------------------------------------------
var cordova = require('cordova');
var exec = require('cordova/exec');
var remoteControls = module.exports;

//------------------------------------------------------------------------------
// Update Functions
//------------------------------------------------------------------------------

//params = [artist, title, album, cover, duration]
// DEPRECATED! Use updateMetadata instead
remoteControls.updateMetas = function(success, fail, params) {
    remoteControls.updateMetadata({
        artist: params[0],
        title: params[1],
        album: params[2],
        cover: params[3],
        duration: params[4]
    }, success, fail);
};

remoteControls.updateMetadata = function(data, success, fail) {
    exec(success, fail, 'RemoteControls', 'updateMetadata', [data]);
};

remoteControls.updatePlayback = function(data, success, fail) {
    exec(success, fail, 'RemoteControls', 'updatePlayback', [data]);
};

remoteControls.updateActions = function(data, success, fail) {
    exec(success, fail, 'RemoteControls', 'updateActions', [data]);
};

//------------------------------------------------------------------------------
// Event Handling
//------------------------------------------------------------------------------

// DEPRECATED (remove it as soon as possible)
remoteControls.receiveRemoteEvent = function(event) {
    var ev = document.createEvent('HTMLEvents');
    ev.remoteEvent = event;
    ev.initEvent('remote-event', true, true, arguments);
    document.dispatchEvent(ev);

    var action = event.subtype;
    if(action == 'prevTrack') {
        action = 'previous';
    } else if(action == 'nextTrack') {
        action = 'next';
    }
    cordova.fireWindowEvent('remotecontrols', {action: action});
};

remoteControls._eventHandler = cordova.addWindowEventHandler('remotecontrols');
remoteControls._eventHandlerRegistered = false;

remoteControls._fireOldEvent = function(obj) {
    var subtype = obj.action;
    if(subtype == 'previous') {
        subtype = 'prevTrack';
    } else if(subtype == 'next') {
        subtype = 'nextTrack';
    }

    var ev = document.createEvent('HTMLEvents');
    ev.remoteEvent = {subtype: subtype};
    ev.initEvent('remote-event', true, true);
    document.dispatchEvent(ev);
};

remoteControls._fireEvent = function(obj) {
    // Fire the event
    cordova.fireWindowEvent('remotecontrols', obj);

    remoteControls._fireOldEvent(obj);
};

remoteControls._eventHandler.onHasSubscribersChange = function() {
    if(remoteControls._eventHandler.numHandlers > 0 && !remoteControls._eventHandlerRegistered) {
        exec(remoteControls._fireEvent, null, 'RemoteControls', 'handleEvents', []);
        remoteControls._eventHandlerRegistered = true;
    }
};
