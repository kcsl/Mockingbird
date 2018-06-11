#!/bin/bash

afl-fuzz -t 5000 -i in_dir -o out_dir ../../fuzzerside/interface @@