package me.rime.templatemod

object TemplateMod {
	fun preInit() {
		println("Pre-initialization")
	}

	fun init() {
		println("Hello world!")
	}

	fun postInit() {
		println("Post-initialization")
	}
}