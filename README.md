# play-nunjucks-spike

This is a library which enables the use of [Nunjucks](https://mozilla.github.io/nunjucks/)
templates in a [Play!](https://www.playframework.com/) web application.

## Installation

__Currently this is an investigation and is not published to an external repository__

To publish the library locally, clone this repository and run:

```$bash
sbt lib/publishLocal
```

Once you have done this you can include it in a local project
by adding the following to your `build.sbt` file:

```scala
libraryDependencies += "uk.gov.hmrc" %% "play-nunjucks-spike" % "0.1.0-SNAPSHOT"
```

## Usage

> This repository contains a reference implementation of how
to use both this library and [govuk-frontend](https://github.com/alphagov/govuk-frontend)
in the `it-server` folder.

Unlike standard Twirl based views in Play! Nunjucks views are
not compiled to Scala code, instead they are 
added as project resources in the `conf` directory of a Play!
application.

### Basic usage

Once the library is installed you should be able to inject a
`NunjucksRenderer` instance to your controllers.

The renderer has a `render` method with the following signature:

```scala
def render[A : OWrites](view: String, context: A)(implicit request: RequestHeader): Try[Html]
```

> There are also overridden versions of this method to render
a view with no context object, or a Play! JSON object.

A simple controller method would look something like:

```scala
def myPage: Action[AnyContent] = Action.async {
  implicit request =>
    
    val context = Json.obj(
      "foo" -> "bar"
    )
  
    Future.fromTry(renderer.render("myView.njk", context)).map(Ok(_))
}
```

As it is quite common for a view to require access to a `Form`
instance we have provided the `NunjucksSupport` trait which
can be mixed in to controllers to provide an `OWrites`instance
for any `Form[_]`.

The output json will be the following:

```json
{
  "myField": {
    "value": "some value or null"
  },
  "myErroredField" : {
    "value": "some value or null",
    "error": {
      "text": "there has been an error!"
    }
  },
  "errors": [
    {
      "text": "there has been an error!",
      "href": "#myErroredField"
    }
  ]
}
```

### Built in helpers

There are some features of Play! that are really useful to be
able to access in views so we've added some helper methods 
that are available to all nunjucks templates.

##### Messages Helper

Play's i18n support is provided through the `Messages` object.
We have added a `messages()` function which delegates to this
object. For example:

```nunjucks
<h1>{{ messages("my.messages.key", "some argument") }}</h1>
```

##### Routes helper

Play has a reverse router which is extremely useful so that
if routes to a particular controller ever change you don't
need to change that reference everywhere in code.

We use Play's `JavaScriptReverseRouter` and make this available
globally under the `routes` object in Nunjucks templates. For example:

```nunjucks
<a href="{{ routes.controllers.MyController.myEndpoint().url }}">my link</a>
```

##### CSRF Helper

Play has built in CSRF support which means that if you have a form
posting back to a Play application, it must have the relevant
CSRF token included in the form payload. We provide a `csrf()`
method which will output a hidden form field with the CSRF
token as a value. For example:

```nunjucks
<form method="post">

  {{ csrf() | safe }}
  
  <input type="submit"></input>
  
</form>
```

> Notice that we have to use the `safe` filter in Nunjucks as this
helper actually outputs HTML

### Dependencies

It is possible to reference templates from dependent webjars as
the library will unpack webjars into a location where their
contents is visible to the application on start. In order to
include something from a dependent library you can use the
following:

```nunjucks
{% from "some-web-jar/some/path" import myComponent %}
```

### Configuration

- `nunjucks.viewPaths` is a list of resource directories to search
for views. By default this is just `views` which means only files in
`conf/views` will be treated as nunjucks sources.

- `nunjucks.libPaths` is a list of directories to search
for webjar libraries. It's useful to set this if you don't want to
specify a deep nested path for each component from a library.