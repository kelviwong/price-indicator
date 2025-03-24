outline of the program

1. take in the price real time per currency pair
2. calculate 1-hour VWAP price.
3. when price comes in, calculate it immediately


For memory usage
1. Storing Price in Deque involve
    12 bytes = object header
    4 bytes currency String
    8 bytes timestamp
    8 bytes price
    8 bytes volume

total = 40 bytes

if one currency have 1000 updates per seconds, memory usage for 1 hour will be 
total number of updates/ hour = 1000 * 60 * 60 = 3600000
total memory usage / hour = 144000000 ~ 144 MB
if there are 1gb memory = 7 currency pairs

2. Storing Price in MMF involve
    8 bytes timestamp
    8 bytes price
    8 bytes volume

total = 24 bytes
if one currency have 1000 updates per seconds, memory usage for 1 hour will be
total number of updates/ hour = 1000 * 60 * 60 = 3600000
total memory usage / hour = 86400000 ~ 86.4 MB
if there are 1gb memory = 11 currency pairs
But suppose we can use much more than memory as we are using SSD disk mapped to portion of memory.