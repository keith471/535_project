# README

## Basic test:
Run main with one config file: `/Users/kstricks/Documents/McGill/4thyear/second_term/535/assignments/proj_code/conf/router1.conf`
Run main again with another config file: `/Users/kstricks/Documents/McGill/4thyear/second_term/535/assignments/proj_code/conf/router2.conf`
Run `attach` for the second Router to attach it to the first one: `attach 127.0.0.1 3001 192.168.1.1 1`
Run `start` for the second Router.
Run `neighbors` on either Router to see that the other is attached.

## Link-state routing test
Start main with router 1: `/Users/kstricks/Documents/McGill/4thyear/second_term/535/assignments/proj_code/conf/router1.conf`
Start main with router 2: `/Users/kstricks/Documents/McGill/4thyear/second_term/535/assignments/proj_code/conf/router2.conf`
Run attach from router 2 to attach it to router 1: `attach 127.0.0.1 3001 192.168.1.1 1`
Run `start` from router 1.
Run `start` from router 2.
From router 1, run `detect 192.168.1.2` to get the shortest path to router 2.
From router 2, run `detect 192.168.1.1` to get the shortest path to router 1.
Also, try finding a router that doesn't exist: from router 1, run `detect 192.168.1.10`.