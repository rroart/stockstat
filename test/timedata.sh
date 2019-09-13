#!/bin/bash

#TIME="[[[[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0]],[[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0],[0,0]]]]"

# non slide input
# one set, two times
#TIME="[[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]]"
TIMELABELS="[[[4 ,5], [14, 15]]]"

TIMECLASS=[[[3,4,5,6,7],[9,10,11,12,13]]]

# two sets, 11 simple times
TIME0="[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]]"
# two sets, three times

TIMECLASS0="[[[3,4,5,6,7]],[[9,10,11,12,13]]]"

# parallells
TIME0="[[[[0,...],[1,...]],[[2,...],[3,...]]], 2nd set]"

TIME0="[[[1,2],[3,4],[5,6]],[[7,8],[9,0],[1,2]]]"
TIMELABELS0="[[[1,2,3]],[[4,5,6]]]" 
#,[[7,8,9]]]"

TIME1="[[[1,2]]]"
TIMELABELS1="[[[3,4,5]]]"
TIMETEST=$TIME0
TIMETESTLABELS=$TIMELABELS0

TIMEARR[1]=$TIME0
TIMEARR[2]=$TIME1
TIMEARRLEN=${#TIMEARR[@]}

TIMELABELS[1]=$TIMELABELS0
TIMELABELS[2]=$TIMELABELS1

TIMETESTARR[1]=$TIME0
TIMETESTARR[2]=$TIME1
TIMETESTARRLEN=${#TIMETESTARR[@]}

TIMETESTLABELSARR[1]=$TIMELABELS0
TIMETESTLABELSARR[2]=$TIMELABELS1

# slide input, output with diff out slide size

TIME0="[[[0, ...],[5, ...]], another set]"
TIMESLIDE="[[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]],[[20,21,22,23,24,25,26,27,28,29,30],[30,31,32,33,34,35,36,37,38,39,40]]]"
TIMESLIDE="[[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20]],[[20,21,22,23,24,25,26,27,28,29,30],[30,31,32,33,34,35,36,37,38,39,40]]]"
TIMESLIDE0="[[[0,1,2,3,4,5,6,7,8,9,10],[10,11,12,13,14,15,16,17,18,19,20],[20,21,22,23,24,25,26,27,28,29,30],[30,31,32,33,34,35,36,37,38,39,40]]]"
TIMESLIDE1="[[[0,1,2,3,4,5,6,7,8,9,10,11],[10,11,12,13,14,15,16,17,18,19,20,21],[20,21,22,23,24,25,26,27,28,29,30,31],[30,31,32,33,34,35,36,37,38,39,40,41]]]"
# turns to 

TIMETESTSLIDE="[[[0,1,2,3,4]],[[10,11,12,13,14]]]"
TIMETESTLABELSSLIDE="[[[7, 8, 9, 10]], [[15, 16, 17, 18]]]"

TIMESLIDEARR[1]=$TIMESLIDE0
TIMESLIDEARR[2]=$TIMESLIDE1
TIMESLIDEARRLEN=${#TIMESLIDEARR[@]}

TIMETESTSLIDEARR[1]=$TIMESLIDE0
TIMETESTSLIDEARR[2]=$TIMESLIDE1
TIMETESTSLIDEARRLEN=${#TIMETESTSLIDEARR[@]}