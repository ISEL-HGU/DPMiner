package edu.handong.csee.isel.data.processor.input;

import edu.handong.csee.isel.data.Input;

public interface InputConverter {

	Input getInputFrom(String[] args);

}
