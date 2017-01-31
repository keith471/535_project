# README

## Basic test:
Run main with one config file: `/Users/kstricks/Documents/McGill/4thyear/second_term/535/assignments/proj_code/conf/router1.conf`
Run main again with another config file: `/Users/kstricks/Documents/McGill/4thyear/second_term/535/assignments/proj_code/conf/router2.conf`
Run `attach` for the second Router to attach it to the first one: `attach 127.0.0.1 3001 192.168.1.1 1`
Run `start` for the second Router.
Run `neighbors` on either Router to see that the other is attached.