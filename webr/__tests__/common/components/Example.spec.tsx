import React from 'react'
import { fromJS } from 'immutable'
import {render, screen} from '@testing-library/react'
import '@testing-library/jest-dom'

import { Main } from  '../../../src/js/common/components/Main'

const fixture = {
  example: {
    result: fromJS({
      testing: 'data',
    }),
  },
};

describe('ExampleView', () => {
  let count = 3;
  it('should render a blank div without data', () => {
    render(<Main props = { count }/>)
  
    /*expect(screen.length).toEqual(1)*/
  })

  it('should render with correct data', () => {
    render(<Main props = { count } {...fixture} />)

    /*expect(screen.length).toEqual(1)
    expect(screen.find('.exampleOutput').length).toEqual(1)*/
  })
})
