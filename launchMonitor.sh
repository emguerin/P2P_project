#!/bin/bash

rt=8000
hPort=8001
mPort=8002
size=100

java -cp bin/ MonitorServer localhost $wPort $mPort

