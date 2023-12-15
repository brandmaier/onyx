/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package gui.graph.decorators;

import java.awt.Graphics;

import gui.graph.Movable;
import gui.graph.Resizable;

public interface DecoratorObject extends Movable, Resizable
{

	int clickX = 0;
	int clickY = 0;
	
	public void paint(Graphics g);

	public String toXML();
}
