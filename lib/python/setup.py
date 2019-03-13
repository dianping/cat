#!/usr/bin/env python
# -*- coding: utf-8 -*-

from setuptools import setup

requirements = [
    "cffi>=1.11,<2.0",
]

classifiers = [
    'Development Status :: 4 - Beta',
    # target user.
    'Intended Audience :: Developers',
    # target python version.
    'Programming Language :: Python :: 2',
    'Programming Language :: Python :: 2.7',
    'Programming Language :: Python :: 3',
    'Programming Language :: Python :: 3.5',
    'Programming Language :: Python :: 3.6',
]

# cython_files = [
#     "src/ccat/ccat.pyx",
# ]

setup(
    name='cat-sdk',
    version='3.1.2',
    author='Cat Team and Contributors',
    author_email='cat@dianping.com',
    url="https://github.com/dianping/cat/tree/master/lib/python",
    license="Apache License 2.0",
    description="Cat SDK for Python",
    long_description=open("README.rst").read(),
    packages=[
            "cat",
    ],
    install_requires=requirements,
    classifiers=classifiers,
    package_dir={'': 'src'},
    package_data={
        'cat':
            [
                "lib/linux-glibc/*.so",
                "lib/linux-musl-libc/*.so",
                "lib/darwin/*.dylib"
            ]
    },
)
