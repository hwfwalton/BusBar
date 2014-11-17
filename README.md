BusBar
======

Creates a persistent notification for checking the arrival times at up to two user defined bus stop quickly.
In Development. Is entirely functional and works with any bus agency using NextBus for their predictions (currently 64 agencies around the country).

Currently working on the UI. Not very intuitive how selecting stops works, and having to choose two is somewhat arbitrary (a holdover from when I'd hardcoded in the two stops I used). Also working on improving the notification. The default layout doesn't make much sense to use. Making a custom one.

Long term ideas:
Use GPS to dynamically choose the closest bus stop to display times for.
Store offline copy of the bus schedule and revert to that when there's limited internet.
