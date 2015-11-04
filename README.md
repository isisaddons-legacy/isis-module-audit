# isis-module-audit #

[![Build Status](https://travis-ci.org/isisaddons/isis-module-audit.png?branch=master)](https://travis-ci.org/isisaddons/isis-module-audit)

This module, intended for use within [Apache Isis](http://isis.apache.org), provides an implementation of Isis' 
`AuditingService3` API that persists audit entries using Isis' own (JDO) objectstore.  Typically this will be to a 
relational database; the module's `AuditEntry` entity is mapped to the "IsisAuditEntry" table.

## Screenshots ##

The following screenshots show an example app's usage of the module.

#### Installing the Fixture Data

Install some sample fixture data ...

![](https://raw.github.com/isisaddons/isis-module-audit/master/images/01-install-fixtures.png)

#### Object with initial (creation) audit entries ####

Because the example entity is annotated with `@DomainObject(auditing = Auditing.ENABLED)`, the initial creation of
that object already results in some audit entries:

![](https://raw.github.com/isisaddons/isis-module-audit/master/images/02-first-object-with-initial-audit-entries.png)

As the screenshot shows, the demo app lists the audit entries for the example entity (a polymorphic association 
utilizing the `BookmarkService`).

#### Audit Entry for each changed property ####

Changing two properties on an object:

![](https://raw.github.com/isisaddons/isis-module-audit/master/images/03-changing-two-properties-on-object.png)

... results in _two_ audit entries created, one for each property:

![](https://raw.github.com/isisaddons/isis-module-audit/master/images/04-two-audit-entries-created.png)

#### Audit entry ####

The audit entry is an immutable record, can also inspect other audit entries created in the same transaction:

![](https://raw.github.com/isisaddons/isis-module-audit/master/images/05-navigate-to-audit-entry-see-other-audit-entries.png)

It is of course also possible to navigate back to audited object:

![](https://raw.github.com/isisaddons/isis-module-audit/master/images/06-navigate-back-to-audited-object.png)


## How to run the Demo App ##

The prerequisite software is:

* Java JDK 8 (>= 1.9.0) or Java JDK 7 (<= 1.8.0)
** note that the compile source and target remains at JDK 7
* [maven 3](http://maven.apache.org) (3.2.x is recommended).

To build the demo app:

    git clone https://github.com/isisaddons/isis-module-audit.git
    mvn clean install

To run the demo app:

    mvn antrun:run -P self-host
    
Then log on using user: `sven`, password: `pass`


## Relationship to Apache Isis Core ##

Isis Core 1.6.0 included the `org.apache.isis.module:isis-module-audit-jdo:1.6.0` Maven artifact.  This module is a
direct copy of that code, with the following changes:

* package names have been altered from `org.apache.isis` to `org.isisaddons.module.audit`
* the `persistent-unit` (in the JDO manifest) has changed from `isis-module-audit-jdo` to 
  `org-isisaddons-module-audit-dom`

Otherwise the functionality is identical; warts and all!

Isis 1.7.0 (and later) no longer ships the `org.apache.isis.module:isis-module-audit-jdo` module; use this addon module instead.


## How to Configure/Use ##

You can either use this module "out-of-the-box", or you can fork this repo and extend to your own requirements. 

#### "Out-of-the-box" ####

To use "out-of-the-box":

* update your classpath by adding this dependency in your project's `dom` module's `pom.xml`:

<pre>
    &lt;dependency&gt;
        &lt;groupId&gt;org.isisaddons.module.audit&lt;/groupId&gt;
        &lt;artifactId&gt;isis-module-audit-dom&lt;/artifactId&gt;
        &lt;version&gt;1.9.0&lt;/version&gt;
    &lt;/dependency&gt;
</pre>

* if using `AppManifest`, then update its `getModules()` method:

<pre>
    @Override
    public List<Class<?>> getModules() {
        return Arrays.asList(
                ...
                org.isisaddons.module.audit.AuditModule.class,
        );
    }
</pre>

* otherwise, update your `WEB-INF/isis.properties`:

<pre>
    isis.services-installer=configuration-and-annotation
    isis.services.ServicesInstallerFromAnnotation.packagePrefix=\
                    ...,\
                    org.isisaddons.module.audit.dom,\
                    ...
</pre>

Notes:
* Check for later releases by searching [Maven Central Repo](http://search.maven.org/#search|ga|1|isis-module-audit-dom).

For audit entries to be created when an object is changed, some configuration is required.  This can be either on a case-by-case basis, or globally:

* by default no object is treated as being audited unless it has explicitly annotated using `@Audited`.  This is the option used in the example app described above.

* alternatively, auditing can be globally enabled by adding a key to `isis.properties`:

<pre>
    isis.services.audit.objects=all
</pre>

An individual entity can then be explicitly excluded from being audited using `@Audited(disabled=true)`.


#### "Out-of-the-box" (-SNAPSHOT) ####

If you want to use the current `-SNAPSHOT`, then the steps are the same as above, except:

* when updating the classpath, specify the appropriate -SNAPSHOT version:

<pre>
    &lt;version&gt;1.10.0-SNAPSHOT&lt;/version&gt;
</pre>

* add the repository definition to pick up the most recent snapshot (we use the Cloudbees continuous integration service).  We suggest defining the repository in a `<profile>`:

<pre>
    &lt;profile&gt;
        &lt;id&gt;cloudbees-snapshots&lt;/id&gt;
        &lt;activation&gt;
            &lt;activeByDefault&gt;true&lt;/activeByDefault&gt;
        &lt;/activation&gt;
        &lt;repositories&gt;
            &lt;repository&gt;
                &lt;id&gt;snapshots-repo&lt;/id&gt;
                &lt;url&gt;http://repository-estatio.forge.cloudbees.com/snapshot/&lt;/url&gt;
                &lt;releases&gt;
                    &lt;enabled&gt;false&lt;/enabled&gt;
                &lt;/releases&gt;
                &lt;snapshots&gt;
                    &lt;enabled&gt;true&lt;/enabled&gt;
                &lt;/snapshots&gt;
            &lt;/repository&gt;
        &lt;/repositories&gt;
    &lt;/profile&gt;
</pre>

#### Forking the repo ####

If instead you want to extend this module's functionality, then we recommend that you fork this repo.  The repo is 
structured as follows:

* `pom.xml   ` - parent pom
* `dom       ` - the module implementation, depends on Isis applib
* `fixture   ` - fixtures, holding a sample domain objects and fixture scripts; depends on `dom`
* `integtests` - integration tests for the module; depends on `fixture`
* `webapp    ` - demo webapp (see above screenshots); depends on `dom` and `fixture`

Only the `dom` project is released to Maven Central Repo.  The versions of the other modules are purposely left at 
`0.0.1-SNAPSHOT` because they are not intended to be released.

## API ##

The `AuditingService3` defines the following API:

    @Programmatic
    public void audit(
            final UUID transactionId, 
            final String targetClass, 
            final Bookmark target, 
            final String memberIdentifier, 
            final String propertyId,
            final String preValue, 
            final String postValue, 
            final String user, 
            final java.sql.Timestamp timestamp);

Isis will automatically call this method on the service implementation if configured.  The method is called often, once 
for every individual property of a domain object that is changed.

## Implementation ##

The `AuditingService3` API is implemented in this module by the `org.isisaddons.module.audit.AuditingService` class.  
This implementation simply persists an audit entry (`AuditEntry`) each time it is called.   This results in a 
fine-grained audit trail.

The `AuditEntry` properties directly correspond to parameters of the `AuditingService3` `audit()` API:

    public class AuditEntry 
        ... 
        private UUID transactionId;
        private String targetClass;
        private String targetStr;
        private String memberIdentifier;
        private String propertyId;
        private String preValue;
        private String postValue;
        private String user;
        private Timestamp timestamp;
        ... 
    }

where:

* `transactionId` is a unique identifier (a GUID) of the transaction in which this audit entry was persisted.
* `timestamp` is the timestamp for the transaction
* `targetClass` holds the class of the audited object, eg `com.mycompany.myapp.Customer`
* `targetStr` stores a serialized form of the `Bookmark`, in other words a provides a mechanism to look up the audited 
  object, eg `CUS:1234` to identify customer with id 1234.  ("CUS" corresponds to the `@ObjectType` annotation/facet).
* `memberIdentifier` is the fully-qualified class and property Id, similar to the way that Javadoc words, eg 
   `com.mycompany.myapp.Customer#firstName`
* `propertyId` is the property identifier, eg `firstName`
* `preValue` holds a string representation of the property's value prior to it being changed.  If the object has been 
  created then it holds the value "[NEW]".  If the string is too long, it will be truncated with ellipses '...'.
* `postValue` holds a string representation of the property's value after it was changed.  If the object has been 
  deleted  then it holds the value "[DELETED]".  If the string is too long, it will be truncated with ellipses '...'.

The combination of `transactionId`, `targetStr` and `propertyId` make up an alternative key to uniquely identify an 
audit entry.  However, there is (deliberately) no uniqueness constraint to enforce this rule.

The `AuditEntry` entity is designed such that it can be rendered on an Isis user interface if required.
    
## Supporting Services ##

As well as the `AuditingService` service (that implements the `AuditingService3` API), the module also provides two 
further domain services:

* The `AuditingServiceMenu` provides actions to search for `AuditEntry`s, underneath an 'Activity' menu on the
secondary menu bar.

* `AuditingServiceRepository` provides the ability to search for persisted (`AuditEntry`) audit entries.  None of its
  actions are visible in the user interface (they are all `@Programmatic`) and so this service is automatically 
  registered.

* `AuditingServiceContributions` provides the `auditEntries` contributed collection to the `HasTransactionId` interface.
  This will therefore display all audit entries that occurred in a given transaction, in other words whenever a command,
  a published event or another audit entry is displayed.

In 1.7.0, it was necessary to explicitly register `AuditingServiceContributions` in `isis.properties`, the
rationale being that this service contributes functionality that appears in the user interface.

In 1.8.x the above policy is reversed: the `AuditingServiceMenu` and `AuditingServiceContributions` services
are both automatically registered, and both provide functionality that will appear in the user interface.  If this is
not required, then either use security permissions or write a vetoing subscriber on the event bus to hide this functionality, eg:

    @DomainService(nature = NatureOfService.DOMAIN)
    public class HideIsisAddonsAuditingFunctionality {

        @Programmatic @PostConstruct
        public void postConstruct() { eventBusService.register(this); }

        @Programmatic @PreDestroy
        public void preDestroy() { eventBusService.unregister(this); }

        @Programmatic @Subscribe
        public void on(final AuditingModule.ActionDomainEvent<?> event) { event.hide(); }

        @Inject
        private EventBusService eventBusService;
    }


## Related Modules/Services ##

As well as defining the `AuditingService3` API, Isis' applib also defines several other closely related services.
Implementations of these services are referenced by the [Isis Add-ons](http://www.isisaddons.org) website.

The `CommandContext` defines the `Command` class which provides request-scoped information about an action 
invocation.  Commands can be thought of as being the cause of an action; they are created "before the fact".  Some 
of the  parameters passed to `AuditingService3` - such as `target`, `user`, and `timestamp` - correspond exactly to the 
`Command` class.

The `CommandService` service is an optional service that acts as a `Command` factory and allows `Command`s to be 
persisted.  `CommandService`'s API introduces the concept of a `transactionId`; once again this is the same
value as is passed to the `AuditingService3`.

The `PublishingService` is another optional service that allows an event to be published when either an object has 
changed or an actions has been invoked.   There are some similarities between publishing to auditing; they both occur 
"after the fact".  However the publishing service's primary use case is to enable inter-system co-ordination (in DDD 
terminology, between bounded contexts).  As such, publishing is much coarser-grained than auditing, and not every 
change need be published.  Publishing also uses the `transactionId`.

The `CommandService` and `PublishingService` are optional; as with the `AuditingService3`, Isis will automatically use 
call each if the service implementation if discovered on the classpath. 

If all these services are configured - such that  commands, audit entries and published events are all persisted, then 
the `transactionId` that is common to all enables seamless navigation between each.  (This is implemented through 
contributed actions/properties/collections; `AuditEntry` implements the `HasTransactionId` interface in Isis' applib, 
and it is this interface that each module has services that contribute to).
 

## Known issues ##

In `1.6.0` through `1.9.x` a call to `DomainObjectContainer#flush()` is required in order that any newly
created objects are populated.  Note that Isis automatically performs a flush prior to any repository call, so in many
cases there may not be any need to call flush explicitly.


## Change Log ##

* `1.9.0` - released against Isis 1.9.0; changed mapped of entities to 'isisaudit' schema; updated to use AppManifest
* `1.8.2` - released against Isis 1.8.0; closes <a href="https://github.com/isisaddons/isis-module-audit/issues/1">#1</a>
* `1.8.1` - released against Isis 1.8.0 (fixed).
* `1.8.0` - released against Isis 1.8.0 (nb: this was a bad release, incorrectly referenced -SNAPSHOT version of Isis core).
* `1.7.0` - released against Isis 1.7.0.
* `1.6.0` - re-released as part of isisaddons, with classes under package `org.isisaddons.module.audit`


## Legal Stuff ##
 
#### License ####

    Copyright 2014-2015 Dan Haywood

    Licensed under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

#### Dependencies ####

There are no third-party dependencies.


##  Maven deploy notes

Only the `dom` module is deployed, and is done so using Sonatype's OSS support (see 
[user guide](http://central.sonatype.org/pages/apache-maven.html)).

#### Release to Sonatype's Snapshot Repo ####

To deploy a snapshot, use:

    pushd dom
    mvn clean deploy
    popd

The artifacts should be available in Sonatype's 
[Snapshot Repo](https://oss.sonatype.org/content/repositories/snapshots).

#### Release to Maven Central ####

The `release.sh` script automates the release process.  It performs the following:

* performs a sanity check (`mvn clean install -o`) that everything builds ok
* bumps the `pom.xml` to a specified release version, and tag
* performs a double check (`mvn clean install -o`) that everything still builds ok
* releases the code using `mvn clean deploy`
* bumps the `pom.xml` to a specified release version

For example:

    sh release.sh 1.10.0 \
                  1.11.0-SNAPSHOT \
                  dan@haywood-associates.co.uk \
                  "this is not really my passphrase"
    
where
* `$1` is the release version
* `$2` is the snapshot version
* `$3` is the email of the secret key (`~/.gnupg/secring.gpg`) to use for signing
* `$4` is the corresponding passphrase for that secret key.

Other ways of specifying the key and passphrase are available, see the `pgp-maven-plugin`'s 
[documentation](http://kohsuke.org/pgp-maven-plugin/secretkey.html)).

If the script completes successfully, then push changes:

    git push origin master
    git push origin 1.10.0

If the script fails to complete, then identify the cause, perform a `git reset --hard` to start over and fix the issue
before trying again.  Note that in the `dom`'s `pom.xml` the `nexus-staging-maven-plugin` has the 
`autoReleaseAfterClose` setting set to `true` (to automatically stage, close and the release the repo).  You may want
to set this to `false` if debugging an issue.
 
According to Sonatype's guide, it takes about 10 minutes to sync, but up to 2 hours to update [search](http://search.maven.org).
            
