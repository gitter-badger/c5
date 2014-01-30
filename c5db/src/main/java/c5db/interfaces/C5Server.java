/*
 * Copyright (C) 2014  Ohm Data
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package c5db.interfaces;

import c5db.ConfigDirectory;
import c5db.messages.generated.CommandReply;
import c5db.messages.generated.ModuleType;
import com.dyuproject.protostuff.Message;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import org.jetlang.channels.Channel;
import org.jetlang.channels.RequestChannel;

import java.util.concurrent.ExecutionException;

/**
 * The root interface for all other modules and modules to get around inside the server.
 * <p/>
 * Provides bootstrapping and other module introspection and management utilities.  Ideally we can run multiple
 * C5Server on the same JVM for testing (may be conflicts with the discovery methods).
 */
public interface C5Server extends Service {
    /**
     * ** Interface type public methods *****
     */

    public long getNodeId();

    // TODO this could be generified if we used an interface instead of ModuleType
    public ListenableFuture<C5Module> getModule(ModuleType moduleType);

    public Channel<Message<?>> getCommandChannel();

    public RequestChannel<Message<?>, CommandReply> getCommandRequests();

    public Channel<ModuleStateChange> getModuleStateChangeChannel();

    public ImmutableMap<ModuleType, C5Module> getModules() throws ExecutionException, InterruptedException;

    public ListenableFuture<ImmutableMap<ModuleType, C5Module>> getModules2();

    public ConfigDirectory getConfigDirectory();

    public static class ModuleStateChange {
        public final C5Module module;
        public final State state;

        @Override
        public String toString() {
            return "ModuleStateChange{" +
                    "module=" + module +
                    ", state=" + state +
                    '}';
        }

        public ModuleStateChange(C5Module module, State state) {
            this.module = module;
            this.state = state;
        }
    }

    public Channel<ConfigKeyUpdated> getConfigUpdateChannel();

    public static class ConfigKeyUpdated {
        public final String configKey;
        public final Object configValue;

        public ConfigKeyUpdated(String configKey, Object configValue) {
            this.configKey = configKey;
            this.configValue = configValue;
        }

        @Override
        public String toString() {
            return "ConfigKeyUpdated{" +
                    "configKey='" + configKey + '\'' +
                    ", configValue=" + configValue +
                    '}';
        }

    }
}