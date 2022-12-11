/**
 * This package contains classes to establish the GUI for the maze game.
 * <p>
 * The main class is Control that together with several State classes implements
 * a State pattern. Control is the context class, each state class includes
 * the code to perform the switch to the next state in the context class.
 * The game goes through the following states: the title state shows the initial
 * screen with the welcome message, it switches to the generating state in which 
 * a maze is generated with the help of the code in the generation package.
 * The game switches then to state playing where the user can play the game, which 
 * eventually leads to state winning that only shows the winning messages and from 
 * where it then switches to the initial title stage again for the next round to play.
 * Main stages in terms of complexity are generating and playing. The code for the 
 * generating stage mainly resides in package generation. 
 * The code for state playing relies on classes such as ColorTheme, CompassRose, 
 * FirstPersonView, Map, and MazePanel to generate the visualization of the maze
 * with a first person view of walls and a separate map view drawn on top of
 * the first person view.
 * </p>
 */
package com.example.amazebyconnormackinnon.gui;