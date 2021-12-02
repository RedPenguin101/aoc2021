#!/bin/bash

day_num=1
while [ $day_num -le 2 ]; do
  padded=$(printf "%02d" $day_num)
  bin=zig-out/bin/day${padded}
  input=resources/day${padded}input.txt

  cat $input | $bin
  let day_num+=1
done

