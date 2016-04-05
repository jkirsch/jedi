package edu.tuberlin.dima.textmining.jedi.core.util;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;

/**
 * Simple Converter that that converts the classname String into the class.
 */
public class JCommanderClassConverter extends BaseConverter<Class> {

    public JCommanderClassConverter(String optionName) {
        super(optionName);
    }

    @Override
    public Class convert(String value) {
        try {
            return Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new ParameterException(getErrorString(value, "a class"));
        }
    }
}
