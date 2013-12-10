package com.lge.metr;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class LocCalculator extends AbstractProcessor<CtClass<?>> {

	@Override
	public void process(CtClass<?> kls) {
		System.out.println(kls.getQualifiedName() + "--");
		for (CtMethod<?> method : kls.getAllMethods()) {
			System.out.println("\t" + method.getSimpleName());
		}
	}

}
