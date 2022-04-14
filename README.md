# ConcurrentLinkedList
Solving problems using a concurrent linked list
Tyler Pearson

Problem 1:

  To run the first problem enter the following commands:
  javac Party.java Servant.java
  java Party

  The output from this program will be the time it took to execute in milliseconds
  and a statement saying that all the thank you cards are done.

  There is a hard coded value for the 500000 presents.
  On average the execution time is around 260ms, but without the contains 1/3rd of
  the time the average execution time is around 170ms.

  For this problem I implemented the lock-free linked list that is provided in the textbook
  and that we went over in class.

  There were some modifications that needed to be made to the linked list. The
  add and contains functions were left alone, but the remove function became a
  removeFirst function where instead of searching for a number we would remove
  just the first item. This allows us to not have to traverse the entire list
  when removing an item. Each servant is represented by a thread and will then either
  add an item, remove an item, or check if an item is in the list. In the problem is
  says alternate the adding and removing but not when to check so I just added it as
  a third part of the loop.

  The bag of unsorted gifts is represented by an array of size n which is filled
  with values from 1 to n and then shuffled to randomly order them. These items are
  then removed from the array by incrementing a counter representing the index.

  Not sure how the servants could have more presents than thank you cards, but we can
  improve the strategy by removing the first item from the linked presents so we
  have the fastest remove times. This significantly increases the time. Also I noticed
  that the run times become extremely long when we are not removing presents from the
  list at the same time as adding because we have to potentially traverse the entire list
  before adding a new present.

Problem 2:

  To run the first problem enter the following commands:
  javac Report.java Sensor.java
  java Report

  The output from this program are the reports of the sensor readings.
  The number of reports and the number of readings per report are hard coded
  values that can be changed as well as the time between readings.

  Hardcoded Values:
    Number of reports: 2
    Number of readings per report: 60 (per sensor/thread)
    Time between readings: 1ms

  For this problem I used the same lock-free linked list as the first problem as well
  as the same removeFirst function to get the lowest readings, but also implemented
  as removeLast function that returns the highest temperature readings. Also kept track
  of the max and min values from each interval of readings to find the max difference in temperature
  for each report.

  This program is pretty efficient as each thread is only adding to the list. Which
  is a majority of the work that is being done. The main thread is doing minor calculations
  and variable updates as well as printing the reports. These thread are using the lock-free
  list so there are no locks causing them to spin, but they do have a sudo lock that causes
  them to wait before getting the next reading. Essentially the main thread release this sort
  of lock and lets all the threads go until they have done one reading.

  I believe this program has a lock-free progress condition as the linked list is lock
  free and the spinning of the locks is only there to allow for the simulation of the problem.

  This program is linearizable each function of the lock-free list is linearizable
  and each thread has an overlapping execution of the add function call meaning the
  list will be correct according to linearizability.
