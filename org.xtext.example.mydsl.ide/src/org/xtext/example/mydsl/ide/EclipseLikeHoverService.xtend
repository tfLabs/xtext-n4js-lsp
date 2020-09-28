package org.xtext.example.mydsl.ide

import com.google.inject.Inject
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider
import org.eclipse.xtext.ide.labels.INameLabelProvider
import org.eclipse.xtext.ide.server.hover.HoverService

class EclipseLikeHoverService extends HoverService {

	@Inject extension IEObjectDocumentationProvider
	@Inject INameLabelProvider nameLabelProvider

	override List<String> getContents(EObject element) {
		val documentation = element.documentation
		if(documentation === null) #[getFirstLine(element)] else #[getFirstLine(element), documentation]
	}

	def String getFirstLine(EObject o) {
		val String label = nameLabelProvider.getNameLabel(o);
		'''«o.eClass().getName()»«IF label !== null» **«label»**«ENDIF»'''
	}
}
