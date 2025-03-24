outline of the program

Java 8

1. take in the price real time per currency pair, e.g. I have created a cmd prompt to input the price when com.run.App start
2. calculate 1-hour VWAP price
3. calculate only when new price come in using sliding windows for 1 hour, e.g. remove old one and add in new price vol.

I have below assumption for my understanding from the pdf.
1. 1 hour worth data VWAP, I assume it means we only publish at least having 1 hour of data.
2. calculate in every price update, so the vwap should have sliding windows for 1 hour and calculate when price update and then shift.
3. there is so requirement on where I should publish, so there are log publisher to final output.
4. assume the price feed come in order of the time we get.

Design:
1. The entry point is in com.run.App.java
2. There is price adaptor which have handler and feeder, validation rule
3. validation rule to avoid some bad data come in.
3. handler is used for parsing the input data
4. feeder is the place to getting data from the data feed.
5. adaptor is only using single thread as it is just a low 
6. Queue is used in which part in order to decouple different component, another way is to use callback register in price event but Queue can decouple it.
7. Queue can be replaced with disruptor (TODO) to avoid some false sharing and locking when notFull and notEmpty.
8. For calculation part, all the calculation is dispatch on same thread for same ccy to avoid context switch and cache.
9. For the significant data in jvm, we only keep the 1 hour of data, which have some calculation like below for memory and as sliding windows will remove anything that expired using Deque.
10. or change MMF which we can set a very large SSD file. TODO: rotate the MMF to expand when not enough space.
11. Factory pattern to be able to create a different object and swap with different factory.
12. we can add few more indicator, like RSI or EMA if close price is there.
13. BackOffQueue to handle when it exceeds the capacity.
14. There is a SimpleRun class in Test package which will inject certain amount of record (e.g. 10000) to test.

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

Build:
1. Simply run the com.run.App main.
2. the command prompt can add price like below:
   addprice 9:30 AM AUD/USD 0.6905 106,198
3. adjust the time and price.