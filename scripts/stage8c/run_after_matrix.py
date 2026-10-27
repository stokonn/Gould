"""Actual NavHost matrix after focused native regressions; all radios stay off."""
from assess import configure, probe, matrix

if __name__=='__main__':
    configure('after')
    for size in ['small','standard']:
        for locale in ['en','cs']:
            for theme in ['Light','Dark']:
                for font in ['1.0','1.3','1.5','2.0']:
                    # The strict startup calibration is separately captured before this run.
                    if (size,locale,theme,font)==('small','en','Light','1.0'):
                        continue
                    matrix.run(locale,theme,font,size=size,with_talkback=size=='small' and (locale,theme,font) in [('en','Light','1.0'),('cs','Dark','1.3'),('en','Dark','2.0'),('cs','Light','1.5')])
    for locale in ['en','cs']:
        for theme in ['Light','Dark']:
            matrix.run(locale,theme,'1.0',scaffold=True,system_dark=(theme!='Dark'))
            # Keep matching NavHost evidence separate from the ordinary matrix.
            old=probe.OUT
            opposite=old/'opposite-system';opposite.mkdir(exist_ok=True)
            probe.OUT=opposite;matrix.OUT=opposite
            try:matrix.run(locale,theme,'1.0',system_dark=(theme!='Dark'))
            finally:probe.OUT=old;matrix.OUT=old
