package com.ecmkit.service.convert;

import com.ecmkit.service.convert.impl.TransformOption;

public interface ImageConvertService {
	public boolean convert(TransformOption option);
	public void CalculateStartAndEndPage(TransformOption option);
}
