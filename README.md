# Pong C Cheats
Pong C Cheats allows you to modify scores in [Pong C](https://github.com/gltchitm/pong-c) to arbitrary values.

> ***WARNING***: Using this program can ruin the Pong C experience. It is for educational purposes only.

> ***WARNING***: This project only works with Pong C, **not** Pong RS or Pong Go

## Project Components
This project is not one program; rather, it is a collection of components designed to be used together to achieve the end goal. These components can be divided into two categories: cheating components, which actually do the modification of scores, and clients, which interface with a cheating component to provide a convienent control panel. All the cheating components and clients included in this project are listed below.

* Cheating Components
    + pongccheatsd
    + pongccheatscli
* Clients
    + pongccheatsgui
    + pongccheatsweb
    + pongccheatstcp

Some of these components have sub-components required to operate. These are listed in the `README.md` files of the components.

## Documentation
Documentation on how pongccheats works can be found in the `README.md` file of pongccheatsd. Documentation for other parts of pongccheats can be found by viewing the `README.md` of the component it pertains to.

## Starting
Each component can be started by running `start.sh` in its directory. Read the component's `README.md` for more information before starting it.

### Quick Start
The following components are a good choice to quickly get started with pongccheats:
* pongccheatsd
* pongccheatsgui

## License
[MIT](LICENSE)
