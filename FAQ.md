## F.A.Q

Q: What are the main differences between Moshidon and Megalodon?

A: There are many, but the most outstanding differences are: the ability to have other server's local timeline inside the app. It can be acessed in the "Add community" option in the top right corner of the Edit timelines screen. Other outstanding features that Moshidon has are some quality of life improvements, such as notification actions and allowing for unlisted replies by default.  Most other features are pretty minor, such as profile notes directly available in the person's profile. Other features are quite minor usability and visibility improvements. All of which can be found in the settings page. 

Q: Will there ever be a version of Moshidon for iOS? 

A: No. As android and iOS apps do not share code, it is incredibly hard to port.

# How To

### Lists & Hashtags

View, edit, and pin lists. Users can be added or removed from lists on their profile page. [ Your Lists icon ]

Follow, pin, and mute hashtags. [ Hashtags you follow icon ]
[ Example of hashtag page with pin and mute icons highlighted ]

### Follow Other Communities

Easily follow another server's local public posts even if your server does not federate with them by adding it as a Community. Search for the server name and pin it to add it to your Home drop down menu. 

[ Server info page with Local Timeline highlighted ]
[ Then local timeline with pin button highlighted ]

### Filtered Posts

Edit your filters without leaving the app. Allows you to have filtered posts collapsed with a warning. [ example filtered post ]

### Notifications

Get only specific types of notifications (no more finished polls!), limit who you get notifications from, or group all notifications into one. 

## Detailed changes

### Features

* [Adding the ability to view other server's local timelines](https://github.com/LucasGGamerM/moshidon/tree/feature/local-timelines)
* [Adding the ability to load followers and following from remote instance](https://github.com/LucasGGamerM/moshidon/tree/feature/remote-followers)
* [Adding the ability to have filtered posts show with a warning](https://github.com/LucasGGamerM/moshidon/tree/feature/filters_again)
* [Add “Unlisted” as a post visibility option](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/enable-unlisted)
  ([Pull request](https://github.com/mastodon/mastodon-android/pull/103))
* Adding a useful private profile note box
* Auto hiding the compose button on scroll
* Adding the ability to remind yourself to add alt text to images
* An indicator for if an image has alt text or not
* Adding the ability to have drafts
* Also adding the ability to view announcements from your instance
* Adding the ability to post for local timeline only (Only on instances that support it!)
* [Add image description button and viewer](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/display-alt-text) ([Pull request](https://github.com/mastodon/mastodon-android/pull/129))
* [Implement pinning posts and displaying pinned posts](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/pin-posts) ([Pull request](https://github.com/mastodon/mastodon-android/pull/140))
* [Implement deleting and re-drafting](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/delete-redraft) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/21))
* [Implement a bookmark button and list](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/bookmarks) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/22))
* [Add “Check for update” button in addition to integrated update checker](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/check-for-update-button)
* [Add “Mark media as sensitive” option](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/mark-media-as-sensitive)
* [Add settings to hide replies and reposts from the timeline](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/filter-home-timeline) ([Pull request](https://github.com/mastodon/mastodon-android/pull/317))
* [Follow and unfollow hashtags](https://github.com/sk22/megalodon/commit/7d38f031f197aa6cefaf53e39d929538689c1e4e) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/233))
* [Notification bell for posts](https://github.com/sk22/megalodon/commit/b166ca705eb9169025ef32bbe6315b42491b57ea) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/81))
* [Viewing lists and adding/removing users from lists](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:list-timeline-views) based on [@obstsalatschuessel](https://github.com/obstsalatschuessel)'s [Pull request](https://github.com/mastodon/mastodon-android/pull/286)
* [List favorited posts](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/favs-list)
* [Accept/reject follow requests](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/follow-requests)
* [Display content warning title above text](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/cw-above-text)
* [Add notifications tab for posts](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/posts-notifications-tab)
* [Show visibility of original post when replying](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/display-reply-visibility)
* [Clickable reply/boost line above posts](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:clickable-boost-reply-line)
* [Clickable reply line while replying to open original post](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/clickable-reply-line-compose)


### Behavior

* Allow for confirmation before reblogging
* Adding a bottom option for the publish button, allowing for easier use on larger screens!
* [Make back button return to the home tab before exiting the app](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/back-returns-home) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/118))
* [Always preserve content warnings when replying](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/always-preserve-cw) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/113))
* [Display full image when adding image description](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/compose-image-description-full-image) ([Pull request](https://github.com/mastodon/mastodon-android/pull/182))
* [Set spoiler height independently to content height](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:spoiler-height-independent) ([Closes issue](https://github.com/mastodon/mastodon-android/issues/166))
* [Option to hide interaction numbers](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:settings/hide-interaction-numbers)
* [Option to always reveal content warnings](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:feature/cw-above-text)
* [Option to disable scrolling title bars](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:settings/disable-marquee)


### Visual

* [Custom extended footer redesign](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:compact-extended-footer)
* [Improvements to the true black mode](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:true-black-improvements)
* [Profile header tweaks](https://github.com/mastodon/mastodon-android/compare/master...sk22:megalodon:ui/profile-header-tweaks)
