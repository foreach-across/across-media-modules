package com.foreach.imageserver.services.transformers;

/**
 * <p>Represents the ImageTransformer feedback on a request for modification:
 * <ul>
 *     <li><strong>UNABLE</strong>: Not able to handle the modification</li>
 *     <li><strong>PREFERRED</strong>: Can and want to do this modification</li>
 *     <li><strong>FALLBACK</strong>: Can do this modification but only try if no preferred transformers available</li>
 * </ul></p>
 */
public enum ImageTransformerPriority
{
	UNABLE,
	PREFERRED,
	FALLBACK
}
