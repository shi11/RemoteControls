//
// RemoteControls.m
// Now Playing Cordova Plugin
//
// Created by François LASSERRE on 12/05/13.
// Copyright 2013 François LASSERRE. All rights reserved.
// MIT Licensed
//

#import "RemoteControls.h"

@implementation RemoteControls

static RemoteControls *remoteControls = nil;

- (void)pluginInitialize
{
    NSLog(@"RemoteControls plugin init.");
}

- (void)updateMetas:(CDVInvokedUrlCommand*)command
{
    NSString *artist = [command.arguments objectAtIndex:0];
    NSString *title = [command.arguments objectAtIndex:1];
    NSString *album = [command.arguments objectAtIndex:2];
    NSString *cover = [command.arguments objectAtIndex:3];
    NSNumber *duration = [command.arguments objectAtIndex:4];
    NSNumber *elapsed = [command.arguments objectAtIndex:5];

    // async cover loading
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        UIImage *image = nil;
        // check whether cover path is present
        if (![cover isEqual: @""]) {
            // cover is remote file
            if ([cover hasPrefix: @"http://"] || [cover hasPrefix: @"https://"]) {
                NSURL *imageURL = [NSURL URLWithString:cover];
                NSData *imageData = [NSData dataWithContentsOfURL:imageURL];
                image = [UIImage imageWithData:imageData];
            }
            // cover is full path to local file
            else if ([cover hasPrefix: @"file://"]) {
                NSString *fullPath = [cover stringByReplacingOccurrencesOfString:@"file://" withString:@""];
                BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:fullPath];
                if (fileExists) {
                    image = [[UIImage alloc] initWithContentsOfFile:fullPath];
                }
            }
            // cover is relative path to local file
            else {
                //check the documents directory
                NSString *basePath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
                NSString *fullPath = [NSString stringWithFormat:@"%@%@", basePath, cover];
                BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:fullPath];
                //check the library directory
                NSString *basePath2 = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
                NSString *fullPath2 = [NSString stringWithFormat:@"%@%@", basePath2, cover];
                BOOL fileExists2 = [[NSFileManager defaultManager] fileExistsAtPath:fullPath2];
                //evaluate if either are true
                if (fileExists) {
                    image = [UIImage imageNamed:fullPath];
                    NSLog(@"Found image in documents");
                } else if (fileExists2) {
                    image = [UIImage imageNamed:fullPath2];
                    NSLog(@"Found image in library");
                }
            }
        }
        else {
            // default named "no-image"
            image = [UIImage imageNamed:@"no-image"];
            NSLog(@"No valid image found");
        }
        // check whether image is loaded
        CGImageRef cgref = [image CGImage];
        CIImage *cim = [image CIImage];
        if (cim != nil || cgref != NULL) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (NSClassFromString(@"MPNowPlayingInfoCenter")) {
                    MPMediaItemArtwork *artwork = [[MPMediaItemArtwork alloc] initWithImage: image];
                    MPNowPlayingInfoCenter *center = [MPNowPlayingInfoCenter defaultCenter];
                    center.nowPlayingInfo = [NSDictionary dictionaryWithObjectsAndKeys:
                        artist, MPMediaItemPropertyArtist,
                        title, MPMediaItemPropertyTitle,
                        album, MPMediaItemPropertyAlbumTitle,
                        artwork, MPMediaItemPropertyArtwork,
                        duration, MPMediaItemPropertyPlaybackDuration,
                        elapsed, MPNowPlayingInfoPropertyElapsedPlaybackTime,
                        [NSNumber numberWithInt:1], MPNowPlayingInfoPropertyPlaybackRate, nil];
                }
            });
        }
    });
}


- (void)receiveRemoteEvent:(UIEvent *)receivedEvent {

    if (receivedEvent.type == UIEventTypeRemoteControl) {

        NSString *subtype = @"other";

        switch (receivedEvent.subtype) {

            case UIEventSubtypeRemoteControlTogglePlayPause:
                NSLog(@"playpause clicked.");
                subtype = @"playpause";
                break;

            case UIEventSubtypeRemoteControlPlay:
                NSLog(@"play clicked.");
                subtype = @"play";
                break;

            case UIEventSubtypeRemoteControlPause:
                NSLog(@"nowplaying pause clicked.");
                subtype = @"pause";
                break;

            case UIEventSubtypeRemoteControlPreviousTrack:
                //[self previousTrack: nil];
                NSLog(@"prev clicked.");
                subtype = @"prevTrack";
                break;

            case UIEventSubtypeRemoteControlNextTrack:
                NSLog(@"next clicked.");
                subtype = @"nextTrack";
                //[self nextTrack: nil];
                break;

            default:
                break;
        }

        NSDictionary *dict = @{@"subtype": subtype};
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options: 0 error: nil];
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        NSString *jsStatement = [NSString stringWithFormat:@"if(window.remoteControls)remoteControls.receiveRemoteEvent(%@);", jsonString];


        if ([self.webView respondsToSelector:@selector(stringByEvaluatingJavaScriptFromString:)]) {
            // Cordova-iOS pre-4
            [self.webView performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:jsStatement) waitUntilDone:NO];
        } else {
            // Cordova-iOS 4+
            [self.webView performSelectorOnMainThread:@selector(evaluateJavaScript:completionHandler:) withObject:jsStatement waitUntilDone:NO];
        }

    }
}

+(RemoteControls *)remoteControls
{
    //objects using shard instance are responsible for retain/release count
    //retain count must remain 1 to stay in mem

    if (!remoteControls)
    {
        remoteControls = [[RemoteControls alloc] init];
    }

    return remoteControls;
}


@end
