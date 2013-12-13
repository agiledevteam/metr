package com.lge.metr

import spoon.support.builder.CtResource
import spoon.AbstractLauncher
import spoon.reflect.Factory

object Loader extends AbstractLauncher {

  def load(res: CtResource): Factory = {
    val f = createFactory();
    val b = f.getBuilder
    b.addInputSource(res)
    b.build
    f
  }

}