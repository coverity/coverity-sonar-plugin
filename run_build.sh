#!/bin/bash

function check_path {
  name=$1
  which $name 2>&1 > /dev/null
  rc=$?
  if [[ $rc == 1 ]]; then
     echo "$name is not in the path"
     exit 1
  fi
}
  
check_path "virtualenv"
check_path "pip"
check_path python

virtualenv `pwd`/py

export VIRTUAL_ENV="`pwd`/py"
export PATH="${VIRTUAL_ENV}/bin:${PATH}"

#pip install -r requirements.txt
echo "
#!/usr/bin/env python

 from setuptools import find_packages, setup

 setup(
     author_email='sig-releng@synopsys.com',
     description='python build tool',
     name='Build',
     scripts = ['build.py'],
     version='1.0',
     zip_safe=False,
 )
" > setup.py

python setup.py install

$* 
