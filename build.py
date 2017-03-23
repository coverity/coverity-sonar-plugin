#/*******************************************************************************
# * Copyright (c) 2017 Synopsys, Inc
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *    Synopsys, Inc - initial implementation and documentation
# *******************************************************************************/
import sys
import subprocess
import re 
import json
import shutil
import os

if __name__ == "__main__":
	# Get version using mvn command
	output = subprocess.Popen("mvn -q -Dexec.executable=echo '-Dexec.args=${project.version}' --non-recursive exec:exec", stdout=subprocess.PIPE, shell=True)
	version = output.stdout.read().strip()

	# Get build number from environment variable BUILD_NUMBER which is provided by jenkins
	build_number = os.path.expandvars("${BUILD_NUMBER}")

	# Get build-id as the push tag
	output = subprocess.Popen("git describe --tags", stdout=subprocess.PIPE, shell=True)	
	build_id = output.stdout.read().strip()

	# git log for the current commit id hash
	output = subprocess.Popen("git log --pretty=format:'%H' -n 1", stdout=subprocess.PIPE, shell=True)
	commit_id = output.stdout.read().strip()

	# Generate the json output text
	json_output = json.dumps({ "commit_id" : commit_id, "build_number" : build_number, "build_id" : build_id, "external_version" : version }, indent=4)

	# Run the typical build for jenkins
	subprocess.check_call("mvn clean verify", shell=True)

	# write the version output file
	version_file = open("./target/coverity-sonar-plugin-{0}.jar.VERSION".format(version),"w")
	version_file.write(json_output)

	# move the plugin .jar file to a versioned file
	shutil.move("./target/coverity-sonar-plugin-{0}.jar".format(version), "./target/coverity-sonar-plugin-{0}.jar".format(version))
