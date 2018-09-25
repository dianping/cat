#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Author: stdrickforce (Tengyuan Fan)
# Email: <stdrickforce@gmail.com> <fantengyuan@meituan.com>

from setuptools import (
    setup,
    find_packages
)

# from distutils.core import Extension

# from Cython.Build import cythonize

# package & upload
# python2.7 setup.py sdist upload -r internal

requirements = [
    "cffi>=1.11,<2.0",
    "psutil>=5.4,<6.0",
]

classifiers = [
    'Development Status :: 4 - Beta',
    # target user.
    'Intended Audience :: Developers',
    # target python version.
    'Programming Language :: Python :: 2',
    'Programming Language :: Python :: 2.6',
    'Programming Language :: Python :: 2.7',
    'Programming Language :: Python :: 3',
    'Programming Language :: Python :: 3.5',
    'Programming Language :: Python :: 3.6',
]

# cython_files = [
#     "src/ccat/ccat.pyx",
# ]

setup(
    name='pycat',
    version='1.0.0',
    author='terence.fan',
    packages=find_packages("src"),
    install_requires=requirements,
    classifiers=classifiers,
    package_dir={'': 'src'},
    package_data={
        'cat':
            [
                "lib/linux/*.so",
                "lib/darwin/*.dylib"
            ]
    },
)
