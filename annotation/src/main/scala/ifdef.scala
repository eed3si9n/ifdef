package com.eed3si9n.ifdef

import scala.annotation.{ meta, StaticAnnotation }

@meta.getter @meta.setter
class ifdef(key: String) extends StaticAnnotation
