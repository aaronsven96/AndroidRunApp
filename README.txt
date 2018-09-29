Lab 4

Aaron Svendsen

My implemtation uses the time that the entry was created as a unique tag. If you create multiple entries with the same time then
it will save over the last entry.

Data is only loaded to the history fragment on fragment start up. SO if a change is made in the database it won't show up until you return
history fragment. 

The social board wasn't working very much so I wasn't able to test it as much as I wanted to but it should be working.

I used emails to distinugish entries between differnet users.

Can only delete entries that our on firebase when the internet is connected

