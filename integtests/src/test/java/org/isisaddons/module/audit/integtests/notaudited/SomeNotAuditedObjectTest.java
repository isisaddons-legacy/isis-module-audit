/*
 *  Copyright 2013~2014 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.isisaddons.module.audit.integtests.notaudited;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.audit.AuditingService3;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.isisaddons.module.audit.dom.AuditEntry;
import org.isisaddons.module.audit.dom.AuditingServiceRepository;
import org.isisaddons.module.audit.fixture.dom.notaudited.SomeNotAuditedObject;
import org.isisaddons.module.audit.fixture.dom.notaudited.SomeNotAuditedObjects;
import org.isisaddons.module.audit.fixture.scripts.AuditDemoAppFixture;
import org.isisaddons.module.audit.integtests.AuditModuleIntegTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SomeNotAuditedObjectTest extends AuditModuleIntegTest {

    @Before
    public void setUpData() throws Exception {
        fixtureScripts.runFixtureScript(new AuditDemoAppFixture(), null);
        transactionService.nextTransaction();
    }

    @Inject
    private FixtureScripts fixtureScripts;

    @Inject
    private SomeNotAuditedObjects someNotAuditedObjects;

    @Inject
    private IsisJdoSupport isisJdoSupport;

    @Inject
    private AuditingService3 auditingService3;

    @Inject
    private AuditingServiceRepository auditingServiceRepository;

    @Inject
    private BookmarkService bookmarkService;

    @Inject
    private TransactionService transactionService;

    @Test
    public void auditEntriesNotCreatedOnCommit() throws Exception {

        // given
        isisJdoSupport.executeUpdate("delete from \"isisaudit\".\"AuditEntry\"");
        transactionService.flushTransaction();

        // when
        wrap(someNotAuditedObjects).create("Faz");

        // currently necessary to ensure that created objects are picked up and enlisted in the transaction as newly created
        transactionService.flushTransaction();

        // then
        final List<Map<String, Object>> prior = isisJdoSupport.executeSql("SELECT * FROM \"isisaudit\".\"AuditEntry\"");
        Assert.assertThat(prior.size(), is(0));

        // when
        this.nextTransaction();

        // then
        Assert.assertThat(prior.size(), is(0));
    }


    @Test
    public void update() throws Exception {

        // given
        final SomeNotAuditedObject someNotAuditedObject = someNotAuditedObjects.listAll().get(0);
        final Bookmark bookmark = bookmarkService.bookmarkFor(someNotAuditedObject);

        assertThat(someNotAuditedObject.getName(), is("Foo"));
        assertThat(someNotAuditedObject.getNumber(), is(nullValue()));

        isisJdoSupport.executeUpdate("delete from \"isisaudit\".\"AuditEntry\"");
        transactionService.flushTransaction();

        // when
        someNotAuditedObject.setName("Bob");
        someNotAuditedObject.setNumber(123);

        this.nextTransaction();

        // then

        final List<AuditEntry> auditEntries = sorted(auditingServiceRepository.findByTargetAndFromAndTo(bookmark, null, null));
        assertThat(auditEntries.size(), is(0));

    }

    private static List<AuditEntry> sorted(List<AuditEntry> auditEntries) {
        Collections.sort(auditEntries, new Comparator<AuditEntry>() {
            @Override
            public int compare(AuditEntry o1, AuditEntry o2) {
                return o1.getMemberIdentifier().compareTo(o2.getMemberIdentifier());
            }
        });
        return auditEntries;
    }


}