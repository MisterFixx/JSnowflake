#JSnowflake

JSnowflake is a java implementation of twitter's Snowflake, which is a network service for generating unique ID numbers at high scale with some simple guarantees.
You can read more about the original project here: https://github.com/twitter-archive/snowflake/tree/snowflake-2010#snowflake

The slight differences on this version of the Snowflake server is that it's much more configurable at startup (check main class for the startup arguments)
JSnowflake is also capable of serving Snowflake IDs at least twice the speed of the original project, if not faster (depends on hardware and network)
In my tests iv'e managed to generated 28,000 snowflakes per second (unlike the original version that could serve < 10,000 Snowflakes per second)

I also wrote a small PHP class to fetch and manipulate snowflakes from this snowflake server, which can be found here: https://gist.github.com/MisterFixx/54570994f44373e89365ba8ca938a7a0