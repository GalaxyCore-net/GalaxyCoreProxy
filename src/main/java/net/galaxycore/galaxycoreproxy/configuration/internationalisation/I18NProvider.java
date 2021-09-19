package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import lombok.Setter;
import net.galaxycore.galaxycorecore.utils.IProvider;

public class I18NProvider implements IProvider<I18NPort> {

    @Setter
    private static I18N i18N;

    @Override
    public I18NPort get() {
        return i18N;
    }

}
