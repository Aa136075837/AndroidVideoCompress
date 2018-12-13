package com.mac.macdocument

/**
 * @author ex-yangjb001
 * @date 2018/12/13.
 */
class FileTypeUtils {
    companion object {
        /**
         * 判断是否支持
         */
        fun isSupportOfficeDoucument(fileName: String): Boolean {
            var fileName = fileName
            // word
            fileName = fileName.toLowerCase()
            return fileName.indexOf(".") > 0 && (fileName.endsWith(Constant.FILE_TYPE_DOC)
                    || fileName.endsWith(Constant.FILE_TYPE_DOCX)
                    || fileName.endsWith(Constant.FILE_TYPE_XLS)
                    || fileName.endsWith(Constant.FILE_TYPE_XLSX)
                    || fileName.endsWith(Constant.FILE_TYPE_PPT)
                    || fileName.endsWith(Constant.FILE_TYPE_PPTX)
                    || fileName.endsWith(Constant.FILE_TYPE_TXT)
                    || fileName.endsWith(Constant.FILE_TYPE_DOT)
                    || fileName.endsWith(Constant.FILE_TYPE_DOTX)
                    || fileName.endsWith(Constant.FILE_TYPE_DOTM)
                    || fileName.endsWith(Constant.FILE_TYPE_XLT)
                    || fileName.endsWith(Constant.FILE_TYPE_XLTX)
                    || fileName.endsWith(Constant.FILE_TYPE_XLTM)
                    || fileName.endsWith(Constant.FILE_TYPE_XLSM)
                    || fileName.endsWith(Constant.FILE_TYPE_POT)
                    || fileName.endsWith(Constant.FILE_TYPE_PPTM)
                    || fileName.endsWith(Constant.FILE_TYPE_POTX)
                    || fileName.endsWith(Constant.FILE_TYPE_POTM))
        }

        /**
         * 判断是否支持
         */
        fun isPDF(fileName: String): Boolean {
            var fileName = fileName
            // pdf
            fileName = fileName.toLowerCase()
            return fileName.indexOf(".") > 0 && fileName.endsWith(Constant.FILE_TYPE_PDF)
        }
    }
}