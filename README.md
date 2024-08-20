# play-nunjucks

This is a library which enables the use of [Nunjucks](https://mozilla.github.io/uk.gov.hmrc.nunjucks/)
templates in a [Play!](https://www.playframework.com/) web application.

### Please note, v1.0.0 onwards of play-nunjucks has NOT been performance tested. Before deploying to production after migrating to v1.0.0 or higher, please ensure your service has been adequately performance tested.

## Installation

sbt
```scala
libraryDependencies += "uk.gov.hmrc" %% "play-nunjucks-play-30" % "x.y.z"
```

## Usage

> This repository contains a reference implementation of how
> to use both this library and [govuk-frontend](https://github.com/alphagov/govuk-frontend)
> in the `it-server` folder.

Unlike standard Twirl based views in Play, Nunjucks views are
not compiled to Scala code, instead they are 
added as project resources in the `conf` directory of a Play
application.

### Basic usage

Once the library is installed you should be able to inject a
`NunjucksRenderer` instance to your controllers.

The renderer has a `render` method with the following signature:

```scala
def render[A : OWrites](view: String, context: A)(implicit request: RequestHeader): Try[Html]
```

> There are also overridden versions of this method to render
> a view with no context object, or a Play! JSON object.

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

There are some features of Play that are really useful to be
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
> helper actually outputs HTML

##### Autoescaping Special Characters

Some characters such as an ampersand (&) are considered special characters. If you have some variable that passes a special character like this `&`, by default play-nunjucks will auto-escape this, displaying an ampersand correctly. If you pass `&amp;` however, you will need to ensure you use the `safe` filter, which will then display `&amp;` as an ampersand.

```
{{ "&amp;" | safe}} - Displays &
{{ "&amp;" }} - Displays &amp;
```

##### Request helper
Play has a `RequestHeader` that is provided to all controllers which contains information
about the current request. Some of this can be extremely useful in views. The following
properties are available within nunjucks views to take advantage of this:
- `request.language` this is the preferred language extracted from the request for example "en" for English
- `request.uri` this contains the uri that the request was for, this includes the query string.
- `request.path` this contains just the path part of the uri that the request was for.
- `request.rawQueryString` this gives just the query string portion of the uri that the request was for.

##### Globals helper
Any key value pairs added in config under `nunjucks.globals` will be accessible in views
by using the globals helper. For example:

application.conf
```hocon
nunjucks.globals {
    foo = "foobar
}
```

someView.njk
```nunjucks
{{ globals.foo }}
```

Would render `foobar`

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

- `nunjucks.routesBatchSize` an int that is used to batch reverse routes in the routes helper. 
This will take effect once your application has more routes than the batch size. Defaults to 500.

- `nunjucks.globals` this is an object of key-value pairs which will be available in
views under `globals`

- `nunjucks.optimizationLevel` an int between -1 and 9 that sets the optimization level of the javascript compiler. Defaults to 0
  - -1 uses interpreted mode, no class files are generated, this uses less memory
  - 0 generates class files but doesn't optimize the javascript, bytecode compiler is fastest in this mode 
  - 1-9 class files are generated and optimizations applied but the bytecode compiler is slower 

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
```shell
sbt "project it-server-play-30" run
```

## Issue with high numbers of routes causing `ArrayIndexOutOfBoundsException`

The routes object referenced in the Routes helper section above is implemented as a Rhino 
[JSFunction](https://mozilla.github.io/rhino/javadoc/index.html?org/mozilla/javascript/annotations/JSFunction.html) in
[NunjucksHelperModule](src/main/scala/uk/gov/hmrc/nunjucks/NunjucksHelperModule.scala) and
has a little complexity behind it.

It relies on play-nunjucks determining a list of `JavaScriptReverseRoute` instances
using reflection in [NunjucksRoutesHelper](src/main/scala/uk/gov/hmrc/nunjucks/NunjucksRoutesHelper.scala)
This `Seq` is passed to Play's `JavaScriptReverseRouter` as documented 
[here](https://www.playframework.com/documentation/2.8.x/ScalaJavascriptRouting). This
sequence is evaluated lazily within a singleton object.

In September 2021 we discovered a bug causing an `ArrayIndexOutOfBoundsException` within
[org.mozilla.classfile.ClassFileWriter$StackMapTable.executeBlock](https://github.com/mozilla/rhino/blob/master/src/org/mozilla/classfile/ClassFileWriter.java#L1728)
that appeared to affect
frontend microservices with high numbers of routes. This appeared to be being caused
by the large size of the `JavaScriptReverseRouter` script, that in some cases exceeded 
3,500 lines of Javascript code. A similar [issue](https://github.com/mozilla/rhino/issues/529) in Rhino was 
reported in March 2019 and [fixed](https://github.com/mozilla/rhino/pull/628)
in December 2019 in [1.7.12](https://github.com/mozilla/rhino/releases/tag/Rhino1_7_12_Release)
However, it's not possible to use this version because Trireme pins Rhino to 1.7.10.

As a workaround, the code in NunjucksHelperModule was modified to build up the routes 
object incrementally by creating and executing multiple JavaScriptReverseRouter scripts containing
batches of 100 routes while deep merging the partial results to obtain the final routes object.
There is an [integration test](it-server/test/uk/gov/hmrc/nunjucks/RoutesSpec.scala)
that demonstrates that the usage of the mergeDeep function preserves
routes at the beginning and end of the routes array.

Maintainers should consider as a future improvement increasing the test coverage for the routes helper 
to cover more examples and edge cases. Additionally, it's worth considering extracting the inline mergeDeep Javascript 
function as a separate Node module to allow it to be independently
tested using a Javascript test runner such as Jest.

Service developers should be aware that this workaround that may have performance implications for your
service and there may be further edge cases that this solution does not cover. 

If your service has a high number of routes and experiences this or a related issue, it's worth
considering whether it's possible to reduce the number of routes via consolidation or potentially
split the frontend microservice into multiple microservices.

## Node Version

The Node version set in the .nvmrc file is used to download nunjucks when this library is built.

It's not used at runtime in services to render templates.

## How to run the tests, for maintainers

1. We need to be using the right version of nodejs, see the section above for more info, if you have NVM installed:
    ```shell
    nvm install
    ```
2. All the tests are in the itServer sub project, run them across all cross-compiled scala versions with:
    ```shell
    env NODE_ENV=production sbt clean +test
    ```
   NODE_ENV set to production causes the npm install via the npm ci command to omit dev dependencies

## Security

We used to use a library called better-npm-audit to run security checks of our dependencies for published vulnerabilities.

It stopped working for this repo because we moved the dependency manifests (package.json and package-lock.json) into the play-nunjucks subfolder, and the tooling we were using to run the tests couldn't run npm in a subfolder.

The main reason for using better-npm-audit was that we could suppress known issues that we couldn't fix (normally where it would require updating/patching a transitive dependency that could break the direct dependency in a way that was hard to check, and where our usage didn't seem to be impacted by the vulnerability.)

We've never had any reported dependency issues for this library, because it only has one dependency (nunjucks).

So rather than try improve the tooling we were using to be able to work with subfolders, we opted to switch to using dependabot. This has the added benefit for us that it's easier for others outside our team to contribute to the maintenance of the library in the future.
