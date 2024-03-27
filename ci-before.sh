#!/usr/bin/env sh

set -e

find . -name pom.xml -print0 | xargs -0 sed -i -E \
-e "s:<revision>dev-SNAPSHOT</revision>:<revision>${REVISION}</revision>:g" \
-e "s:<version>dev-SNAPSHOT</version>:<version>${ACROSS_FRAMEWORK_VERSION}</version>:g" \
-e "s:<across-framework.version>dev-SNAPSHOT</across-framework.version>:<across-framework.version>${ACROSS_FRAMEWORK_VERSION}</across-framework.version>:g" \
-e "s:<across-autoconfigure.version>dev-SNAPSHOT</across-autoconfigure.version>:<across-autoconfigure.version>${ACROSS_AUTOCONFIGURE_VERSION}</across-autoconfigure.version>:g" \
-e "s:<across-base-modules.version>dev-SNAPSHOT</across-base-modules.version>:<across-base-modules.version>${ACROSS_BASE_MODULES_VERSION}</across-base-modules.version>:g" \
-e "s:<across-entity-admin-modules.version>dev-SNAPSHOT</across-entity-admin-modules.version>:<across-entity-admin-modules.version>${ACROSS_ENTITY_ADMIN_MODULES_VERSION}</across-entity-admin-modules.version>:g" \
-e "s:<across-user-auth-modules.version>dev-SNAPSHOT</across-user-auth-modules.version>:<across-user-auth-modules.version>${ACROSS_USER_AUTH_MODULES_VERSION}</across-user-auth-modules.version>:g" \
