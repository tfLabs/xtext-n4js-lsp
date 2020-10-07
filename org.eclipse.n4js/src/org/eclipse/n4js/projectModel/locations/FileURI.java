/**
 * Copyright (c) 2019 NumberFour AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   NumberFour AG - Initial API and implementation
 */
package org.eclipse.n4js.projectModel.locations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.List;

import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.URI;
//import org.eclipse.n4js.N4JSGlobals;
import org.eclipse.n4js.utils.URIUtils;
//import org.eclipse.n4js.utils.io.FileDeleter;
import org.eclipse.n4js.utils.OSInfo;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;

@SuppressWarnings("javadoc")
public final class FileURI {

	private File cachedFile;
	private final URI wrapped;

	public FileURI(URI wrapped) {
		this.wrapped = validate(wrapped);
	}

	public FileURI(File file) {
		this(toFileURI(file));
		this.cachedFile = file;
	}

	private static URI toFileURI(File file) {
		String absolutePath = file.getAbsolutePath();
		URI fileURI = URI.createFileURI(absolutePath);
		if (fileURI.authority() == null) {
			fileURI = URI.createHierarchicalURI(fileURI.scheme(), "", fileURI.device(), fileURI.segments(),
					fileURI.query(), fileURI.fragment());
		}
		return fileURI;
	}

	protected URI validate(URI given) throws IllegalArgumentException, NullPointerException {
		Preconditions.checkNotNull(given);
		List<String> segments = given.segmentsList();

		final int segCountMax = given.hasTrailingPathSeparator() ? segments.size() - 1 : segments.size();
		for (int i = 0; i < segCountMax; i++) {
			String segment = segments.get(i);
			Preconditions.checkArgument(segment.length() > 0, "'%s'", given);
			if (OSInfo.isWindows()) {
				Preconditions.checkArgument(!segment.contains(File.separator));
			}
		}

		return given;
	}

	/**
	 * Append the given segment to this location. The result is normalized.
	 */
	public final FileURI appendSegment(String segment) {
		return appendSegments(segment);
	}

	/**
	 * Append the given segments to this location. The result is normalized.
	 */
	public final FileURI appendSegments(String... segments) {
		return appendRelativeURI(URI.createHierarchicalURI(segments, null, null));
	}

	private FileURI appendRelativeURI(URI relativeURI) {
		String[] segments = relativeURI.segments();
		if (segments.length == 0) {
			throw new IllegalArgumentException("Cannot append empty URI.");
		}
		if (!URI.validSegments(segments)) {
			throw new IllegalArgumentException(String.valueOf(relativeURI));
		}
		if (segments.length == 1 && segments[0].isEmpty()) {
			throw new IllegalArgumentException("Use withTrailingPathDelimiter instead");
		}
		for (int i = 0; i < segments.length - 1; i++) {
			if (segments[i].isEmpty()) {
				throw new IllegalArgumentException("Cannot add intermediate empty segments");
			}
		}
		URI base = withTrailingPathDelimiter().toURI();
		URI result = relativeURI.resolve(base);
		return createFrom(result);
	}

	protected FileURI createFrom(URI uri) {
		return new FileURI(uri);
	}

	public final FileURI withTrailingPathDelimiter() {
		URI uri = toURI();
		if (uri.hasTrailingPathSeparator()) {
			@SuppressWarnings("unchecked")
			FileURI result = (FileURI) this;
			return result;
		}
		return createFrom(uri.appendSegment(""));
	}

	public URI toURI() {
		return wrapped;
	}
	
	public FileURI toFileURI() {
		return this;
	}


}
