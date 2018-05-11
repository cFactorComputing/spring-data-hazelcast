package test.utils.repository.custom;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.hazelcast.repository.support.SimpleHazelcastRepository;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.repository.core.EntityInformation;

import test.utils.domain.MyTitle;

/**
 * <P>Implement a custom repository for {@link MyTitleRepository}, but
 * inherit most of the behaviour from {@link SimpleHazelcastRepository}.
 * </P>
 * 
 * @author Neil Stevenson
 *
 * @param <T> The domain object
 * @param <ID> The key of the domain object
 */
public class MyTitleRepositoryImpl<T extends Serializable, ID extends Serializable>
	extends SimpleHazelcastRepository<T, ID>
	implements MyTitleRepository<T, ID> {
	
	public MyTitleRepositoryImpl(EntityInformation<T, ID> metadata, KeyValueOperations keyValueOperations) {
		super(metadata, keyValueOperations);
	}

	/**
	 * <P>
	 * Count the words in a particular title.
	 * </P>
	 * 
	 * @param year to lookup
	 * @return Tokens in string, -1 if not found
	 */
	public int wordsInTitle(String year) {
		@SuppressWarnings("unchecked")
		Optional<MyTitle> myTitle = (Optional<MyTitle>) super.findById((ID) year);

		if (!myTitle.isPresent()) {
			return -1;
		} else {
			return myTitle.get().getTitle().split("[-\\s]").length;
		}
	}

}
