# play-nunjucks

This is a library which enables the use of [Nunjucks](https://mozilla.github.io/uk.gov.hmrc.nunjucks/)
templates in a [Play!](https://www.playframework.com/) web application.

## Installation

sbt
```scala
libraryDependencies += "uk.gov.hmrc" %% "play-nunjucks" % "x.y.z-play-27"
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

Please be aware that this helper does not handle [nested form values](https://www.playframework.com/documentation/2.8.x/ScalaForms#Nested-values). If you have a form constraint on a
composite field, you will need to either implement an alternative OWrites[Form[_]] or
remap the form error keys before passing to your Nunjucks template to ensure the correct error links are emitted
as per [GDS guidance](https://design-system.service.gov.uk/components/error-summary/).

For example, if you have a form that uses the `DateInput` view model from [play-nunjucks-viewmodel](https://github.com/hmrc/play-nunjucks-viewmodel),
and wish to perform validation on the `date.day` field, you will need to remap any form errors on the 
`date.day` field to the `date` field because in the DateInput viewmodel, the day text input has an id corresponding to the
`date` field rather than the `date.day` field.

Such a workaround might look something like this:

```scala
renderer.render(
  template = "template.njk",
  ctx = Json.obj(
    "form"       -> formWithErrors.copy(
      errors = formWithErrors.errors map { e => 
        if (e.key == "date.day") e.copy(key = "date") else e 
      }
    )
  )
).map(BadRequest(_))
```

### Built in helpers

There are some features of Play! that are really useful to be
able to access in views so we've added some helper methods 
that are available to all nuk.gov.hmrc.nunjuckstemplates.

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
#### Paths
- `nunjucks.viewPaths` is a list of resource directories to search
for views. By default this is just `views` which means only files in
`conf/views` will be treated as nuk.gov.hmrc.nunjuckssources.

- `nunjucks.libPaths` is a list of directories to search
for webjar libraries. It's useful to set this if you don't want to
specify a deep nested path for each component from a library.

- `nunjucks.noCache` a boolean that when true disables caching of precompiled templates.
Should be set to false in Production for maximum performance.

#### Logging
Adding the following logging configuration to your logback.xml will suppress noise generated by the trireme library:

```xml
  <logger name="io.apigee.trireme" level="INFO"/>
  <logger name="org.webjars" level="INFO"/>
```

## Using the library from an MDTP microservice

Please read the [following guide](/docs/getting-started-mdtp.md) for instructions on how to use
the library on an MDTP microservice.

### Nunjucks rendering exceptions
When this library is consumed by an MDTP microservice and a Nunjucks
page has a bug which raises an error when rendered and viewed at run
time, this will be sent into the application log at ERROR level with
the relevant file, location in file and stack trace.

It may be obscured amongst other logs when running locally but will
be there. When this occurs in an MDTP environment, you should be
able to find this among your application logs in Kibana with a
relevant field `exception` and the stack trace therewithin.

## Example server
An it-server project has been included as a reference, providing an example page.
You can see this in action by running the following command:
```sbt
PLAY_VERSION=2.7 sbt "project itServer" run
