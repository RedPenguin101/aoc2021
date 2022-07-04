# Day 16: Packet Decoder

Jesus, a _long_ question.

"a method of packing numeric expressions into a binary sequence...saved in hexadecimal" 

i.e. one hex digit is four binary digits: 0 -> 0000, F -> 1111 (15 in dec) etc.

The input consists of _packets_. A packet can contain other packets.

Each packet starts with a 2x3 bit **header**: _version_ and _type id_, of 3 bits each.

## Literals 
**Packet Type 4 (100)**: packet is _literal value_, which just contains a binary number.

```
nibble: 111 122 22333 34444 55556 666
binary: 110 100 10111 11110 00101 000
catgy:  VVV TTT SAAAA SAAAA sAAAA GGG

  V=version
  T=type
  S=signal bit 'not last'
  s=signal bit 'last'
  A=output
  G=garbage

  _0111 _1110 _0101
=> 0111  1110  0101 => 2021
```

## Operators

**Other packet types** represent an _operator_ that performs a calculation on subpackets.

```
Version + OperatorType + LTI + Length + Subpacket + LTI2 + Length2 + Subpacket2 + ...
```

## Length and length type indicator

A LTI of 0 means the length of the subpacket is indicated by length: next 15 bits are a number representing the total length in bits of the sub-packets.


```
                                  Subpacket1     Subpacket2
         LTI=0  l=27bits        
001 110   0     000000000011011   110 100 01010  010 100 10001 00100 0000000
VVV TTT   I     LLLLLLLLLLLLLLL 
                                  VVV TTT sAAAA  VVV TTT SBBBB sBBBB GGGGGGG
                                  1010=10        10100=20
```

A LTI of 1 means that the next 11 bits are a number giving the number of subpackets immediately contained in this packet.

```
        LTI=1  L=3sps       SP1            SP2            SP3
111 011   1    00000000011  010 100 00001  100 100 00010  001 100 00011 00000
VVV TTT   I    LLLLLLLLLLL
                            VVV TTT sAAAA  VVV TTT sBBBB  VVV TTT sCCCC GGGGG
```

```
      8-- -A- - -0---0---4- --A --- 8 ---0---1--- A-- -8- - -0---0---2---F- --4 --- 7---8 ---
      100 010 1 00000000001 001 010 1 00000000001 101 010 0 000000000001011 110 100 01111 000
pk1   VVV TTT S LLLLLLLLLLL VVV TTT S LLLLLLLLLLL VVV TTT s LLLLLLLLLLLLLLL VVV TTT sAAAA ggg
      4                     1                     5                         6
```

```
            1          2         3
123 456 7 89012345678 901234567890123
     6-- -2- - -0---0---8- | --0 --- 0 ---0---1---6--- | 1-- -1- --5-- | -6- --2 ---C- | --8 --- 8 ---0---2--- | 1---1---8-- | -E---3---4- | --
     011 000 1 00000000010 | 000 000 0 000000000010110 | 000 100 01010 | 101 100 01011 | 001 000 1 00000000010 | 00010001100 | 01110001101 | 00
Pk1  VVV TTT S LLLLLLLLLLL | VVV TTT b LLLLLLLLLLLLLLL | VVV TTT sAAAA | VVV TTT sAAAA | VVV TTT B LLLLLLLLLLL | VVVTTTsAAAA | VVVTTTsAAAA | 
                                                  22
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
