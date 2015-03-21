#!/usr/bin/env bash
echo "Deploying javadoc..."

TDIR=`mktemp -d -t javadoc`

echo "Creating temporary directory..."
mkdir -v $TDIR/javadoc-latest

echo "Copying files to temporary directory..."
cp -R build/docs/javadoc $TDIR/javadoc-latest

cd $TDIR

echo "Cloning current gh-pages branch..."
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/a11n/redmine-java-api gh-pages > /dev/null

cd gh-pages

echo "Cleaning..."
git rm -rf . > /dev/null

echo "Copying javadocs to gh-pages..."
cp -Rf $TDIR/javadoc-latest/javadoc/ .
git add -f .
git commit -m "Latest javadoc auto-pushed to gh-pages." > /dev/null
git push -fq origin gh-pages > /dev/null

echo "javadoc deployed!"

echo "Removing temporary files..."
rm -rf $TDIR > /dev/null
