# Day 16: Packet Decoder

Jesus, a _long_ question.

"a method of packing numeric expressions into a binary sequence...saved in hexadecimal"

0 -> 0000, F -> 1111 (15 in dec) etc.

The input has _packets_. Each packet starts with a 2x3 bit **header**: version and type id, 3 bits each

Type 4 (100): packet is _literal value_, just contains a binary number.

```
nibble: 111 122 22333 34444 55556 666
binary: 110 100 10111 11110 00101 000
categy: VVV TTT SAAAA SAAAA sAAAA GGG
=> 011111100101 => 2021

  V=version
  T=type
  S=signal bit 'not last'
  s=signal but 'last'
  A=output
  G=garbage
```
Non literals contain a _length type indicator_, LTI

LTI 0: _packet container by length_: next 15 bits are a number representing the total length in bits of the sub-packets.

LTI 1: _packet container by number of packets_

```
     LTIv  15bits   l=27bits 
111 122 2233334444555566 66777788889 99900001111222233334444
001 110 0000000000011011 11010001010 01010010001001000000000
VVV TTT ILLLLLLLLLLLLLLL AAAAAAAAAAA BBBBBBBBBBBBBBBB
                         VVVTTTsAAAA VVVTTTSBBBBsBBBB
                                =10          10100=20
```

```
        11 bits  3sp
111 122 223333444455 55666677778 88899990000 1111222233334444
111 011 100000000011 01010000001 10010000010 0011000001100000
VVV TTT ILLLLLLLLLLL AAAAAAAAAAA BBBBBBBBBBB CCCCCCCCCCC
                     VVVTTTsAAAA VVVTTTsBBBB VVVTTTsCCCC
                               1           2           3
```

## Part 1

**Decode the structure of your hexadecimal-encoded BITS transmission; what do you get if you add up the version numbers in all packets?**

So we need to divide the input up into packets. The problem is we don't know how big the packets are when we parse them, so we need to do it on the fly.

The size is also an issue. The 'number' represented by the input is _big_. We can't just parse it. We'll need to do it as a stream.

Let's say we have a stream `D2FE28` We parse the first two chars `D2` into `110 100 10`. We get the V and T 3bits, and a `10` left over.

Next we need to grab 5 digits (including the `10` we have in queue). So we decode `F` to `1111`, giving `10111 1` This bit length mismatch is very irritating. Maybe some kind of function which you can apply to a seq of chars to get x-digits? Like `(take-bin coll 5)`.  Don't like it. Ugly. There is those trailing zero's at the end, so there it _some_ operation that happens at the hex level. Each package in total is in 4bit nibbles.

The minimum packet length would be something like `F1E` `111 100 01111 0`, or 3 nibbles. That is, a literal packet representing a number below 16. That last 0 would be thrown away.

So I think what we're going to have to have here is a `parse-package :: stream -> [package(s) stream]`. Then the termination condition is when there's nothing left of the stream.

For compounds, the bits prior to the sub packages are `3+3+1+11=18` or `3+3+1+15=22`. Neither of which are divisible by 4. Annoying

For literals:

```
nibble: 111 122 22333 34444 55556 666
nibpos: 123 412 34123 41234 12341 234
binary: 110 100 10111 11110 00101 000
categy: VVV TTT SAAAA SAAAA sAAAA GGG
```

The first signal bit comes on the 3rd bit of the 2nd nibble. The first result is the last bit of the 2nd nibble and the first 3 bytes of the 3rd nibble.
